package simpledb;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Knows how to compute some aggregate over a set of StringFields.
 */
public class StringAggregator implements Aggregator {

    private static final long serialVersionUID = 1L;

    /**
     * Aggregate constructor
     * @param gbfield the 0-based index of the group-by field in the tuple, or NO_GROUPING if there is no grouping
     * @param gbfieldtype the type of the group by field (e.g., Type.INT_TYPE), or null if there is no grouping
     * @param afield the 0-based index of the aggregate field in the tuple
     * @param what aggregation operator to use -- only supports COUNT
     * @throws IllegalArgumentException if what != COUNT
     */
    private final int gbFieldID;
    private final Type gbFieldTypes;
    private int aField;
    private final Op op;
    private HashMap<Field,Integer> hashMap;

    public StringAggregator(int gbfield, Type gbfieldtype, int afield, Op what) {
        // some code goes here
        this.op = what;
        this.gbFieldID = gbfield;
        this.gbFieldTypes = gbfieldtype;
        this.aField = afield;
        //make sure that this.op is the same as op.COUNT
        assert(op.equals(Op.COUNT));
        this.hashMap = new HashMap<Field, Integer>();
    }

    /**
     * Merge a new tuple into the aggregate, grouping as indicated in the constructor
     * @param tup the Tuple containing an aggregate field and a group-by field
     */
    public void mergeTupleIntoGroup(Tuple tup) {
        // some code goes here
        Field field_groups;
        if (gbFieldID == Aggregator.NO_GROUPING){
            field_groups = null;
        } else{
            field_groups = tup.getField(gbFieldID);
        }

        // increment the counter in the hashmap to take track of the groups
        if (!hashMap.containsKey(field_groups))
        {
            hashMap.put(field_groups, 0);
        }

        int counter = hashMap.get(field_groups);
        hashMap.put(field_groups, counter+1);
    }

    /**
     * Create a DbIterator over group aggregate results.
     *
     * @return a DbIterator whose tuples are the pair (groupVal,
     *   aggregateVal) if using group, or a single (aggregateVal) if no
     *   grouping. The aggregateVal is determined by the type of
     *   aggregate specified in the constructor.
     */
    public DbIterator iterator() {
        // some code goes here
        //create a tupledesc grouped
        String[] names;
        Type[] types;
        TupleDesc td;
        // if no grouping/ with grouping
        if (gbFieldID == Aggregator.NO_GROUPING) {
            td = new TupleDesc(new Type[] {Type.INT_TYPE}, new String[] {"aggregateValue"});
        }
        else {
            td = new TupleDesc(new Type[] {gbFieldTypes, Type.INT_TYPE}, new String[] {"groupValue", "aggregateValue"});
        }


        ArrayList<Tuple> tuples = new ArrayList<Tuple>();
        Tuple addMe;
        for (Field group : hashMap.keySet()) {
            int aggregateVal;
            if (op == Op.AVG) {
                aggregateVal = hashMap.get(group) / this.hashMap.get(group);
            }
            else {
                aggregateVal = hashMap.get(group);
            }
            addMe = new Tuple(td);
            if (gbFieldID == Aggregator.NO_GROUPING){
                addMe.setField(0, new IntField(aggregateVal));
            }
            else {
                addMe.setField(0, group);
                addMe.setField(1, new IntField(aggregateVal));
            }
            tuples.add(addMe);
        }
        return new TupleIterator(td, tuples);
    }

}
