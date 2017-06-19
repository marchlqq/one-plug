package com.pingan.oneplug.util;

import android.content.pm.Signature;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * apk 签名解析
 *
 */
public final class SignatureParser {

    /**
     * DEBUG 开关
     */
    public static final boolean DEBUG = true;
    /**
     * TAG
     */
    public static final String TAG = "SignatureParser";

    /**
     * 缓存管理的锁
     */
    private static Object mSync = new Object();
    /**
     * 缓存池
     */
    private static WeakReference<byte[]> mReadBuffer;

    /**
     * 不能实例化
     */
    private SignatureParser() {

    }

    private static Collection<? extends Certificate> getSignature(CertificateFactory x509CertFactoryImpl,
                                                                  InputStream inputStream) {
        try {
            return x509CertFactoryImpl.generateCertificates(inputStream);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            return null;
        }
    }

    public static Signature[] collectCertificatesWithoutCheck(String mArchiveSourcePath) {
        CertificateFactory x509CertFactoryImpl = null;
        try {
            x509CertFactoryImpl = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            e.printStackTrace();
            return collectCertificates(mArchiveSourcePath, true);
        }

        ZipFile zipFile = null;
        InputStream inputStream = null;
        Collection<Certificate> localCerts = null;
        try {
            zipFile = new ZipFile(mArchiveSourcePath);
            ZipEntry zipEntry;
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            String name;
            boolean isMeetPublicKey = false;
            while (entries.hasMoreElements()) {
                zipEntry = entries.nextElement();
                name = zipEntry.getName();
                if (name.startsWith("META-INF/") && (name.endsWith(".RSA") || name.endsWith(".DSA"))) {
                    isMeetPublicKey = true;
                    inputStream = zipFile.getInputStream(zipEntry);

                    Collection<Certificate> certs = (Collection<Certificate>) getSignature(
                            x509CertFactoryImpl, inputStream);
                    if (certs != null) {
                        if (localCerts == null) {
                            localCerts = certs;
                        } else {
                            localCerts.addAll(certs);
                        }
                    }

                } else if (isMeetPublicKey && !name.startsWith("META-INF/")) {
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (localCerts != null && localCerts.size() > 0) {
            LinkedList<Signature> signatures = new LinkedList<Signature>();
            for (Certificate cert : localCerts) {
                try {
                    signatures.add(new Signature(cert.getEncoded()));
                } catch (CertificateEncodingException e) {
                    e.printStackTrace();
                    continue;
                }
            }
            Signature[] signatureArray = new Signature[signatures.size()];
            return signatures.toArray(signatureArray);
        }
        return collectCertificates(mArchiveSourcePath, true);
    }

    /**
     * 获取apk签名
     *
     * @param mArchiveSourcePath apk 文件
     * @return 签名，null表示解析签名失败
     */
    public static Signature[] collectCertificates(String mArchiveSourcePath, boolean onlyCheckExecFiles) {
        Signature[] signatures = null;

        WeakReference<byte[]> readBufferRef;
        byte[] readBuffer = null;
        synchronized (mSync) {
            readBufferRef = mReadBuffer;
            if (readBufferRef != null) {
                mReadBuffer = null;
                readBuffer = readBufferRef.get();
            }
            if (readBuffer == null) {
                readBuffer = new byte[8192];
                readBufferRef = new WeakReference<byte[]>(readBuffer);
            }
        }

        try {
            JarFile jarFile = new JarFile(mArchiveSourcePath);

            Certificate[] certs = null;

            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                final JarEntry je = entries.nextElement();
                if (je.isDirectory())
                    continue;

                final String name = je.getName();

                if (name.startsWith("META-INF/"))
                    continue;

                if (onlyCheckExecFiles) {
                    if (!name.endsWith(".dex") && !name.endsWith(".so")) {
                        continue;
                    }
                }

                final Certificate[] localCerts = loadCertificates(jarFile, je, readBuffer);

                if (localCerts == null) {
                    Log.e(TAG, "Package " + mArchiveSourcePath + " has no certificates at entry "
                            + je.getName() + "; ignoring!");
                    jarFile.close();
                    return null;
                } else if (certs == null) {
                    certs = localCerts;
                } else {
                    // Ensure all certificates match.
                    for (int i = 0; i < certs.length; i++) {
                        boolean found = false;
                        for (int j = 0; j < localCerts.length; j++) {
                            if (certs[i] != null && certs[i].equals(localCerts[j])) {
                                found = true;
                                break;
                            }
                        }
                        if (!found || certs.length != localCerts.length) {
                            jarFile.close();
                            return null;
                        }
                    }
                }
            }
            jarFile.close();

            synchronized (mSync) {
                mReadBuffer = readBufferRef;
            }

            if (certs != null && certs.length > 0) {
                final int N = certs.length;
                signatures = new Signature[certs.length];
                for (int i = 0; i < N; i++) {
                    signatures[i] = new Signature(certs[i].getEncoded());
                }
            } else {
                Log.e(TAG, "Package " + mArchiveSourcePath + " has no certificates; ignoring!");
                return null;
            }
        } catch (CertificateEncodingException e) {
            Log.w(TAG, "Exception reading " + mArchiveSourcePath, e);
            return null;
        } catch (IOException e) {
            Log.w(TAG, "Exception reading " + mArchiveSourcePath, e);
            return null;
        } catch (RuntimeException e) {
            Log.w(TAG, "Exception reading " + mArchiveSourcePath, e);
            return null;
        }

        return signatures;
    }

    /**
     * 加载签名文件
     *
     * @param jarFile    jar文件
     * @param je         Jar Entry
     * @param readBuffer 读取Buffer
     * @return 签名文件
     */
    private static Certificate[] loadCertificates(JarFile jarFile, JarEntry je, byte[] readBuffer) {
        try {
            // We must read the stream for the JarEntry to retrieve
            // its certificates.
            InputStream is = new BufferedInputStream(jarFile.getInputStream(je));
            while (is.read(readBuffer, 0, readBuffer.length) != -1) {
                // not using
            }
            is.close();
            return je != null ? je.getCertificates() : null;
        } catch (IOException e) {
            Log.w(TAG, "Exception reading " + je.getName() + " in " + jarFile.getName(), e);
        } catch (RuntimeException e) {
            Log.w(TAG, "Exception reading " + je.getName() + " in " + jarFile.getName(), e);
        }
        return null;
    }

}
