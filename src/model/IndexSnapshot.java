package model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Sergey Saiyan
 * @version $Id$
 */
public final class IndexSnapshot {

    public Date date;
    public double value;

    protected static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public IndexSnapshot(Date date, double value) {
        this.date = date;
        this.value = value;
    }

    @Override
    public String toString() {
        return "IndexSnapshot {" +
                "date=" + dateFormat.format(date) +
                ", value=" + value +
                '}';
    }
}
