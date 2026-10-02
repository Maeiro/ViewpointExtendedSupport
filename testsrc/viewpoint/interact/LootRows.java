package viewpoint.interact;

import java.util.ArrayList;
import java.util.List;

public final class LootRows {
    private final ArrayList<Row> rows = new ArrayList<>();
    private int selected;

    public void addHeading(String value) {
        Row row = new Row();
        row.heading = value;
        rows.add(row);
    }

    public void addAction(String value, int action) {
        Row row = new Row();
        row.name = value;
        row.action = action;
        rows.add(row);
    }

    public List<Row> rows() {
        return rows;
    }

    public static final class Row {
        private String heading;
        private String name;
        private int action = -1;
        private boolean enabled = true;
        private Object icon;

        public String heading() {
            return heading;
        }

        public String name() {
            return name;
        }

        public int action() {
            return action;
        }

        public Object icon() {
            return icon;
        }

        public void setIcon(Object value) {
            icon = value;
        }
    }
}
