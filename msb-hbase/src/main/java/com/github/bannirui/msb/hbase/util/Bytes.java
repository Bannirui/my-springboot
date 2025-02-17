package com.github.bannirui.msb.hbase.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import sun.misc.Unsafe;

public class Bytes {
    private static final boolean UNSAFE_UNALIGNED = UnsafeAvailChecker.unaligned();

    public static long toLong(byte[] bytes) {
        return toLong(bytes, 0, 8);
    }

    public static long toLong(byte[] bytes, int offset, int length) {
        if (length == 8 && offset + length <= bytes.length) {
            if (UNSAFE_UNALIGNED) {
                return toLongUnsafe(bytes, offset);
            } else {
                long l = 0L;
                for (int i = offset; i < offset + length; ++i) {
                    l <<= 8;
                    l ^= (long) (bytes[i] & 255);
                }
                return l;
            }
        } else {
            throw explainWrongLengthOrOffset(bytes, offset, length, 8);
        }
    }

    public static long toLongUnsafe(byte[] bytes, int offset) {
        return Bytes.LexicographicalComparerHolder.UnsafeComparer.littleEndian ? Long.reverseBytes(
            Bytes.LexicographicalComparerHolder.UnsafeComparer.theUnsafe.getLong(bytes,
                (long) offset + (long) Bytes.LexicographicalComparerHolder.UnsafeComparer.BYTE_ARRAY_BASE_OFFSET)) :
            Bytes.LexicographicalComparerHolder.UnsafeComparer.theUnsafe.getLong(bytes,
                (long) offset + (long) Bytes.LexicographicalComparerHolder.UnsafeComparer.BYTE_ARRAY_BASE_OFFSET);
    }

    private static IllegalArgumentException explainWrongLengthOrOffset(byte[] bytes, int offset, int length, int expectedLength) {
        String reason;
        if (length != expectedLength) {
            reason = "Wrong length: " + length + ", expected " + expectedLength;
        } else {
            reason = "offset (" + offset + ") + length (" + length + ") exceed the capacity of the array: " + bytes.length;
        }
        return new IllegalArgumentException(reason);
    }

    public static boolean toBoolean(byte[] b) {
        if (b.length != 1) {
            throw new IllegalArgumentException("Array has wrong size: " + b.length);
        } else {
            return b[0] != 0;
        }
    }

    public static short toShort(byte[] bytes) {
        return toShort(bytes, 0, 2);
    }

    public static short toShort(byte[] bytes, int offset, int length) {
        if (length == 2 && offset + length <= bytes.length) {
            if (UNSAFE_UNALIGNED) {
                return toShortUnsafe(bytes, offset);
            } else {
                short n = 0;
                n = (short) (n ^ bytes[offset] & 255);
                n = (short) (n << 8);
                n = (short) (n ^ bytes[offset + 1] & 255);
                return n;
            }
        } else {
            throw explainWrongLengthOrOffset(bytes, offset, length, 2);
        }
    }

    public static short toShortUnsafe(byte[] bytes, int offset) {
        return Bytes.LexicographicalComparerHolder.UnsafeComparer.littleEndian ? Short.reverseBytes(
            Bytes.LexicographicalComparerHolder.UnsafeComparer.theUnsafe.getShort(bytes,
                (long) offset + (long) Bytes.LexicographicalComparerHolder.UnsafeComparer.BYTE_ARRAY_BASE_OFFSET)) :
            Bytes.LexicographicalComparerHolder.UnsafeComparer.theUnsafe.getShort(bytes,
                (long) offset + (long) Bytes.LexicographicalComparerHolder.UnsafeComparer.BYTE_ARRAY_BASE_OFFSET);
    }

    public static int toInt(byte[] bytes) {
        return toInt(bytes, 0, 4);
    }

    public static int toInt(byte[] bytes, int offset) {
        return toInt(bytes, offset, 4);
    }

    public static int toInt(byte[] bytes, int offset, int length) {
        if (length == 4 && offset + length <= bytes.length) {
            if (UNSAFE_UNALIGNED) {
                return toIntUnsafe(bytes, offset);
            } else {
                int n = 0;
                for (int i = offset; i < offset + length; ++i) {
                    n <<= 8;
                    n ^= bytes[i] & 255;
                }
                return n;
            }
        } else {
            throw explainWrongLengthOrOffset(bytes, offset, length, 4);
        }
    }

    public static int toIntUnsafe(byte[] bytes, int offset) {
        return Bytes.LexicographicalComparerHolder.UnsafeComparer.littleEndian ? Integer.reverseBytes(
            Bytes.LexicographicalComparerHolder.UnsafeComparer.theUnsafe.getInt(bytes,
                (long) offset + (long) Bytes.LexicographicalComparerHolder.UnsafeComparer.BYTE_ARRAY_BASE_OFFSET)) :
            Bytes.LexicographicalComparerHolder.UnsafeComparer.theUnsafe.getInt(bytes,
                (long) offset + (long) Bytes.LexicographicalComparerHolder.UnsafeComparer.BYTE_ARRAY_BASE_OFFSET);
    }

    public static float toFloat(byte[] bytes) {
        return toFloat(bytes, 0);
    }

    public static float toFloat(byte[] bytes, int offset) {
        return Float.intBitsToFloat(toInt(bytes, offset, 4));
    }

    public static double toDouble(byte[] bytes) {
        return toDouble(bytes, 0);
    }

    public static double toDouble(byte[] bytes, int offset) {
        return Double.longBitsToDouble(toLong(bytes, offset, 8));
    }

    public static BigDecimal toBigDecimal(byte[] bytes) {
        return toBigDecimal(bytes, 0, bytes.length);
    }

    public static BigDecimal toBigDecimal(byte[] bytes, int offset, int length) {
        if (bytes != null && length >= 5 && offset + length <= bytes.length) {
            int scale = toInt(bytes, offset);
            byte[] tcBytes = new byte[length - 4];
            System.arraycopy(bytes, offset + 4, tcBytes, 0, length - 4);
            return new BigDecimal(new BigInteger(tcBytes), scale);
        } else {
            return null;
        }
    }

    public static byte[] toBytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    public static byte[][] toBytesArray(List<String> strList) {
        if (CollectionUtils.isEmpty(strList)) {
            return null;
        }
        int size = strList.size();
        byte[][] columns = new byte[size][];
        for (int i = 0; i < size; ++i) {
            columns[i] = toBytes(strList.get(i));
        }
        return columns;
    }

    public static byte[] toBytes(long l) {
        byte[] b = new byte[8];
        for (int i = 7; i > 0; --i) {
            b[i] = (byte) ((int) l);
            l >>>= 8;
        }
        b[0] = (byte) ((int) l);
        return b;
    }

    public static byte[] toBytes(Boolean b) {
        return new byte[] {(byte) (b ? -1 : 0)};
    }

    public static byte[] toBytes(short val) {
        byte[] b = new byte[] {0, (byte) val};
        val = (short) (val >> 8);
        b[0] = (byte) val;
        return b;
    }

    public static byte[] toBytes(int val) {
        byte[] b = new byte[4];
        for (int i = 3; i > 0; --i) {
            b[i] = (byte) val;
            val >>>= 8;
        }
        b[0] = (byte) val;
        return b;
    }

    public static byte[] toBytes(float f) {
        return toBytes(Float.floatToRawIntBits(f));
    }

    public static byte[] toBytes(double d) {
        return toBytes(Double.doubleToRawLongBits(d));
    }

    public static byte[] toBytes(BigDecimal val) {
        byte[] valueBytes = val.unscaledValue().toByteArray();
        byte[] result = new byte[valueBytes.length + 4];
        int offset = putInt(result, 0, val.scale());
        putBytes(result, offset, valueBytes, 0, valueBytes.length);
        return result;
    }

    public static int putInt(byte[] bytes, int offset, int val) {
        if (bytes.length - offset < 4) {
            throw new IllegalArgumentException("Not enough room to put an int at offset " + offset + " in a " + bytes.length + " byte array");
        } else if (UNSAFE_UNALIGNED) {
            return putIntUnsafe(bytes, offset, val);
        } else {
            for (int i = offset + 3; i > offset; --i) {
                bytes[i] = (byte) val;
                val >>>= 8;
            }
            bytes[offset] = (byte) val;
            return offset + 4;
        }
    }

    public static int putBytes(byte[] tgtBytes, int tgtOffset, byte[] srcBytes, int srcOffset, int srcLength) {
        System.arraycopy(srcBytes, srcOffset, tgtBytes, tgtOffset, srcLength);
        return tgtOffset + srcLength;
    }

    public static int putIntUnsafe(byte[] bytes, int offset, int val) {
        if (Bytes.LexicographicalComparerHolder.UnsafeComparer.littleEndian) {
            val = Integer.reverseBytes(val);
        }
        Bytes.LexicographicalComparerHolder.UnsafeComparer.theUnsafe.putInt(bytes,
            (long) offset + (long) Bytes.LexicographicalComparerHolder.UnsafeComparer.BYTE_ARRAY_BASE_OFFSET, val);
        return offset + 4;
    }

    interface Comparer<T> {
        int compareTo(T var1, int var2, int var3, T var4, int var5, int var6);
    }

    static class LexicographicalComparerHolder {

        enum UnsafeComparer implements Bytes.Comparer<byte[]> {
            INSTANCE;
            static final Unsafe theUnsafe;
            static final int BYTE_ARRAY_BASE_OFFSET;
            static final boolean littleEndian;

            UnsafeComparer() {
            }

            static boolean lessThanUnsignedLong(long x1, long x2) {
                if (littleEndian) {
                    x1 = Long.reverseBytes(x1);
                    x2 = Long.reverseBytes(x2);
                }
                return x1 + -9223372036854775808L < x2 + -9223372036854775808L;
            }

            static boolean lessThanUnsignedInt(int x1, int x2) {
                if (littleEndian) {
                    x1 = Integer.reverseBytes(x1);
                    x2 = Integer.reverseBytes(x2);
                }
                return ((long) x1 & 4294967295L) < ((long) x2 & 4294967295L);
            }

            static boolean lessThanUnsignedShort(short x1, short x2) {
                if (littleEndian) {
                    x1 = Short.reverseBytes(x1);
                    x2 = Short.reverseBytes(x2);
                }
                return (x1 & '\uffff') < (x2 & '\uffff');
            }

            public int compareTo(byte[] buffer1, int offset1, int length1, byte[] buffer2, int offset2, int length2) {
                if (buffer1 == buffer2 && offset1 == offset2 && length1 == length2) {
                    return 0;
                }
                int minLength = Math.min(length1, length2);
                int minWords = minLength / 8;
                long offset1Adj = (long) (offset1 + BYTE_ARRAY_BASE_OFFSET);
                long offset2Adj = (long) (offset2 + BYTE_ARRAY_BASE_OFFSET);
                int j = minWords << 3;
                int offset;
                for (offset = 0; offset < j; offset += 8) {
                    long lw = theUnsafe.getLong(buffer1, offset1Adj + (long) offset);
                    long rw = theUnsafe.getLong(buffer2, offset2Adj + (long) offset);
                    long diff = lw ^ rw;
                    if (diff != 0L) {
                        return lessThanUnsignedLong(lw, rw) ? -1 : 1;
                    }
                }
                offset = j;
                int a;
                int b;
                if (minLength - j >= 4) {
                    a = theUnsafe.getInt(buffer1, offset1Adj + (long) j);
                    b = theUnsafe.getInt(buffer2, offset2Adj + (long) j);
                    if (a != b) {
                        return lessThanUnsignedInt(a, b) ? -1 : 1;
                    }
                    offset = j + 4;
                }
                if (minLength - offset >= 2) {
                    short sl = theUnsafe.getShort(buffer1, offset1Adj + (long) offset);
                    short sr = theUnsafe.getShort(buffer2, offset2Adj + (long) offset);
                    if (sl != sr) {
                        return lessThanUnsignedShort(sl, sr) ? -1 : 1;
                    }
                    offset += 2;
                }
                if (minLength - offset == 1) {
                    a = buffer1[offset1 + offset] & 255;
                    b = buffer2[offset2 + offset] & 255;
                    if (a != b) {
                        return a - b;
                    }
                }
                return length1 - length2;
            }

            static {
                if (Bytes.UNSAFE_UNALIGNED) {
                    theUnsafe = UnsafeAccess.theUnsafe;
                    BYTE_ARRAY_BASE_OFFSET = theUnsafe.arrayBaseOffset(byte[].class);
                    if (theUnsafe.arrayIndexScale(byte[].class) != 1) {
                        throw new AssertionError();
                    } else {
                        littleEndian = ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN);
                    }
                } else {
                    throw new Error();
                }
            }
        }
    }
}
