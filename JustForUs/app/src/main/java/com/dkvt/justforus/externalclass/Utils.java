package com.dkvt.justforus.externalclass;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by 08670_000 on 26/05/2015.
 */
public class Utils {
    public static void CopyStream(InputStream paramInputStream,
                                  OutputStream paramOutputStream) {
        try {
            byte[] arrayOfByte = new byte[1024];
            while (true) {
                int i = paramInputStream.read(arrayOfByte, 0, 1024);
                if (i == -1)
                    return;
                paramOutputStream.write(arrayOfByte, 0, i);
            }
        } catch (Exception localException) {
        }
    }
}
