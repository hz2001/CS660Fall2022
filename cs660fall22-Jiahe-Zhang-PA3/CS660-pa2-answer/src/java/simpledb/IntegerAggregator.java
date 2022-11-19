package simpledb;

import java.util.Collections;
import java.util.Formattable;
import java.util.HashMap;
import java.util.LinkedList;
/**
 * Knows how to compute some aggregate over a set of IntFields.
 */
public class IntegerAggregator implements Aggregator {

    private static final long serialVersionUID = 1L;

    /**
     * Aggregate constructor
     * 
     * @param gbfield
     *            the 0-based index of the group-by field in the tuple, or
     *            NO_GROUPING if there is no grouping
     * @param gbfieldtype
     *            the type of the group by field (e.g., Type.INT_TYPE), or null
     *            if there is no grouping
     * @param afield
     *            the 0-based index of the aggregate field in the tuple
     * @param what
     *            the aggregation operator
     */

    private int gbFieldID;
    private Type gbFieldTypes;
    private int aField;
    private Op op;
    private HashMap<Field,AggregateFields> hashMap;
    public IntegerAggregator(int gbfield, Type gbfieldtype, int afield, Op what) {
        // some code goes here
        this.op = what;
        this.gbFieldID = gbfield;
        this.gbFieldTypes = gbfieldtype;
        this.aField = afield;
        //make sure that this.op is the same as op.COUNT
        this.hashMap = new HashMap<Field, AggregateFields>();
    }

    /**
     * Merge a new tuple into the aggregate, grouping as indicated in the
     * constructor
     * 
     * @param tup
     *            the Tuple containing an aggregate field and a group-by field
     */
    public void mergeTupleIntoGroup(Tuple tup) {
        // some code goes here
        Field groupVal = null;
        if (this.gbFieldID != NO_GROUPING) {
            groupVal = tup.getField(gbFieldID);
        }
        AggregateFields aggFields = hashMap.get(groupVal);
        if (aggFields == null)
            aggFields = new AggregateFields(groupVal);

        int x = ((IntField) tup.getField(aField)).getValue();

        aggFields.count++;
        aggFields.sum += x;
        aggFields.min = (x < aggFields.min ? x : aggFields.min);
        aggFields.max = (x > aggFields.max ? x : aggFields.max);
        if (op==Op.SC_AVG)
            aggFields.sumCount+=((IntField) tup.getField(aField+1)).getValue();

        hashMap.put(groupVal, aggFields);

    }

    /**
     * Create a DbIterator over group aggregate results.
     * 
     * @return a DbIterator whose tuples are the pair (groupVal, aggregateVal)
     *         if using group, or a single (aggregateVal) if no grouping. The
     *         aggregateVal is determined by the type of aggregate specified in
     *         the constructor.
     */
    public DbIterator iterator() {
        // some code goes here
        LinkedList<Tuple> result = new LinkedList<Tuple>();
        int aggField = 1;
        TupleDesc td;


        if (this.gbFieldID == NO_GROUPING) {
            if (this.op==Op.SUM_COUNT)
                td = new TupleDesc(new Type[]{Type.INT_TYPE, Type.INT_TYPE});
            else
                td = new TupleDesc(new Type[] { Type.INT_TYPE });
            aggField = 0;
        } else {
            if (this.op==Op.SUM_COUNT)
                td = new TupleDesc(new Type[]{gbFieldTypes,Type.INT_TYPE, Type.INT_TYPE});
            else
                td = new TupleDesc(new Type[] { gbFieldTypes, Type.INT_TYPE });
        }

        // iterate over groups and create summary tuples
        for (Field groupVal : hashMap.keySet()) {
            AggregateFields aggFields = hashMap.get(groupVal);
            Tuple tup = new Tuple(td);

            if (this.gbFieldID != NO_GROUPING) {
                if (this.gbFieldTypes == Type.INT_TYPE)
                    tup.setField(0, groupVal);
                else
                    tup.setField(0, new StringField(groupVal.toString(), Type.STRING_LEN));
            }
            switch (this.op) {
                case MIN:
                    tup.setField(aggField, new IntField(aggFields.min));
                    break;
                case MAX:
                    tup.setField(aggField, new IntField(aggFields.max));
                    break;
                case SUM:
                    tup.setField(aggField, new IntField(aggFields.sum));
                    break;
                case COUNT:
                    tup.setField(aggField, new IntField(aggFields.count));
                    break;
                case AVG:
                    tup.setField(aggField, new IntField(aggFields.sum / aggFields.count));
                    break;
                case SUM_COUNT:
                    tup.setField(aggField, new IntField(aggFields.sum));
                    tup.setField(aggField+1, new IntField(aggFields.count));
                    break;
                case SC_AVG:
                    tup.setField(aggField, new IntField(aggFields.sum / aggFields.sumCount));
                    break;
                }

            result.add(tup);
        }

        DbIterator retVal = null;
        retVal = new TupleIterator(td, Collections.unmodifiableList(result));
        return retVal;

    }

    private class AggregateFields {
        public Field groupVal;
        public int min, max, sum, count, sumCount;

        public AggregateFields(Field groupVal) {
            this.groupVal = groupVal;
            min = Integer.MAX_VALUE;
            max = Integer.MIN_VALUE;
            sum = count = sumCount = 0;
        }
    }

}
