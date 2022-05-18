package Utils;

import java.util.Comparator;

public class AidClass {
    public String Aid;
    public String Name;
    public int PriorityIndicator;
    public String Adfname;
    public String kernelID;
    public String extdSelection;

    public static Comparator<AidClass> SortByAid = new Comparator<AidClass>() {
        @Override
        public int compare(AidClass o1, AidClass o2) {
            return (o1.PriorityIndicator - o2.PriorityIndicator);
        }
    };

}
