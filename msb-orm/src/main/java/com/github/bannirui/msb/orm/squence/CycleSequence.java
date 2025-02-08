package com.github.bannirui.msb.orm.squence;

import com.github.bannirui.msb.ex.FrameworkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CycleSequence extends AbstractSequence {
    private static final Logger logger = LoggerFactory.getLogger(CycleSequence.class);
    private Long today = null;
    private Long minValue = 0L;
    private Long maxValue = 999999999L;
    private DataSource dataSource;
    private String name;

    public CycleSequence(String name, DataSource dataSource) {
        this.name = name;
        this.dataSource = dataSource;
        this.today = this.getToDay();
    }

    private Long getToDay() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
        return Long.parseLong(sdf.format(new Date()));
    }

    @Override
    public long nextValue(int type) {
        Long oldday = this.today;
        long value = this.nextValue();
        if (!oldday.equals(this.today)) {
            oldday = this.today;
            value = this.nextValue();
            if (!oldday.equals(this.today)) {
                throw FrameworkException.getInstance("Sequence切换重试发生异常,原Sequence[{}],新Sequence[{}]", oldday, this.today);
            }
        }
        value = this.today * 1000000000000L + value * 1000L + (long)(type * 10L);
        return CycleSequence.Luhn.gen(value);
    }

    @Override
    String getSequenceName() {
        this.today = this.getToDay();
        return this.name + this.today;
    }

    @Override
    protected boolean isInitRange() {
        return super.getCurrentRange() == null || this.getToDay() > this.today;
    }

    @Override
    SequenceDao getSequenceDao() {
        DefaultSequenceDao dao = new DefaultSequenceDao(this.dataSource);
        dao.setMinValue(this.minValue);
        dao.setMaxValue(this.maxValue);
        return dao;
    }

    static class Luhn {
        public static Long gen(Long number) {
            String sortNumber = sort(number);
            int sum = 0;
            for(int i = 0; i < sortNumber.length() - 1; ++i) {
                int e = Integer.parseInt(String.valueOf(sortNumber.charAt(i)));
                if (i != sortNumber.length() - 1 && i % 2 == 1) {
                    e = e << 1;
                    e = e > 9 ? e - 9 : e;
                }
                sum = sum + e;
            }
            return number + (sum * 9L % 10);
        }

        private static String sort(Long number) {
            int sortIndex = (int)(number % 10000L / 1000L);
            if (sortIndex == 0) {
                return number.toString();
            } else {
                String numberStr = number.toString();
                StringBuilder sb = new StringBuilder();
                sb.append(numberStr.substring(0, 1));
                sb.append(numberStr.substring(sortIndex, numberStr.length() - 1));
                sb.append(numberStr.substring(1, sortIndex));
                sb.append(numberStr.substring(numberStr.length() - 1, numberStr.length()));
                return sb.toString();
            }
        }

        public static boolean check(Long number) {
            String sortNumber = sort(number);
            Integer sum = 0;
            for(int i = 0; i < sortNumber.length(); ++i) {
                Integer e = Integer.valueOf(String.valueOf(sortNumber.charAt(i)));
                if (i != sortNumber.length() - 1 && i % 2 == 1) {
                    e = e << 1;
                    e = e > 9 ? e - 9 : e;
                }
                sum = sum + e;
            }
            return sum % 10 == 0;
        }
    }
}
