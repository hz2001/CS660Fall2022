package simpledb;

import java.util.*;

/**
 * The Join operator implements the relational join operation.
 */
public class Join extends Operator {

    private static final long serialVersionUID = 1L;
    private JoinPredicate jp;
    private DbIterator dbItor1;
    private DbIterator dbItor2;
    private TupleDesc combined;
    private Tuple tuple = null;
    /**
     * Constructor. Accepts to children to join and the predicate to join them
     * on
     * 
     * @param p
     *            The predicate to use to join the children
     * @param child1
     *            Iterator for the left(outer) relation to join
     * @param child2
     *            Iterator for the right(inner) relation to join
     */
    public Join(JoinPredicate p, DbIterator child1, DbIterator child2) {
        // some code goes here
        this.jp = p;
        this.dbItor1 = child1;
        this.dbItor2 = child2;
        // also has to assign for the combined tupledesc td
        this.combined = TupleDesc.merge(dbItor1.getTupleDesc(),dbItor2.getTupleDesc());
    }

    public JoinPredicate getJoinPredicate() {
        // some code goes here
        return this.jp;
    }

    /**
     * @return
     *       the field name of join field1. Should be quantified by
     *       alias or table name.
     * */
    public String getJoinField1Name() {
        // some code goes here
        int joinField1 = this.jp.getField1();
        return this.dbItor1.getTupleDesc().getFieldName(joinField1);
    }

    /**
     * @return
     *       the field name of join field2. Should be quantified by
     *       alias or table name.
     * */
    public String getJoinField2Name() {
        // some code goes here
        int joinField2 = this.jp.getField2();
        return this.dbItor2.getTupleDesc().getFieldName(joinField2);
    }

    /**
     * @see simpledb.TupleDesc#merge(TupleDesc, TupleDesc) for possible
     *      implementation logic.
     */
    public TupleDesc getTupleDesc() {
        // some code goes here
        return this.combined;
    }

    public void open() throws DbException, NoSuchElementException,
            TransactionAbortedException {
        // some code goes here
        this.dbItor1.open();
        this.dbItor2.open();
        super.open();
    }

    public void close() {
        // some code goes here
        super.close();
        this.dbItor2.close();
        this.dbItor1.close();

    }

    public void rewind() throws DbException, TransactionAbortedException {
        // some code goes here
        this.dbItor1.rewind();
        this.dbItor2.rewind();
    }

    /**
     * Returns the next tuple generated by the join, or null if there are no
     * more tuples. Logically, this is the next tuple in r1 cross r2 that
     * satisfies the join predicate. There are many possible implementations;
     * the simplest is a nested loops join.
     * <p>
     * Note that the tuples returned from this particular implementation of Join
     * are simply the concatenation of joining tuples from the left and right
     * relation. Therefore, if an equality predicate is used there will be two
     * copies of the join attribute in the results. (Removing such duplicate
     * columns can be done with an additional projection operator if needed.)
     * <p>
     * For example, if one tuple is {1,2,3} and the other tuple is {1,5,6},
     * joined on equality of the first column, then this returns {1,2,3,1,5,6}.
     * 
     * @return The next matching tuple.
     * @see JoinPredicate#filter
     */
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        // some code goes here
        // not so sure
        while(tuple != null || dbItor1.hasNext()){
            // case: tuple == null, meaning dbItor has next:
            if (tuple == null){
                assert dbItor1.hasNext();
                tuple = dbItor1.next();
            }
//    		System.out.print("tuple: " + tuple.toString());
            while(dbItor2.hasNext()){
                Tuple tuple2 = dbItor2.next();
//    		    System.out.print("tuple2: " + tuple2.toString());

                if (jp.filter(tuple,tuple2)){
                    //merge the two tuples and return the new tuple
                    Tuple newTuple = new Tuple(this.getTupleDesc());
                    int counter = 0;
                    for (int i = 0; i < dbItor1.getTupleDesc().numFields(); i++) {
                        newTuple.setField(counter, tuple.getField(i));
                        counter++;
                    }
                    for (int i = 0; i < dbItor2.getTupleDesc().numFields(); i++) {
                        newTuple.setField(counter, tuple2.getField(i));
                        counter++;
                    }
                    return newTuple;
                }
            }
            tuple = null;
            //update dbItor2
            dbItor2.rewind();

        }
        return null;
    }

    @Override
    public DbIterator[] getChildren() {
        // some code goes here
        return new DbIterator[] { this.dbItor1, this.dbItor2 };
    }

    @Override
    public void setChildren(DbIterator[] children) {
        // some code goes here
        this.dbItor1 = children[0];
        this.dbItor2 = children[1];
    }

}
