package com.dvc.utils;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.UUID;

public class KeyGenerator {
    public static long generateSyskey() {
        final UUID uid = UUID.randomUUID();
        final ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(uid.getLeastSignificantBits());
        buffer.putLong(uid.getMostSignificantBits());
        final BigInteger bi = new BigInteger(buffer.array());
        return Math.abs(bi.longValue());
    }

    public static long generateID() {
        Random random = new Random();
        return 100000000 + random.nextInt(900000000);
    }
}
