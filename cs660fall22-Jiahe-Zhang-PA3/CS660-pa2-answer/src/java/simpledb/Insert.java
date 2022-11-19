package simpledb;

import javax.xml.crypto.Data;
import java.io.IOException;

/**
 * Inserts tuples read from the child operator into the tableId specified in the
 * constructor
 */
public class Insert extends Operator {

    private static final long serialVersionUID = 1L;
    private final TransactionId transId;
    private DbIterator dbItor;
    private final int tableId;
    private final TupleDesc td;
    private boolean finished = false;

    /**
     * Constructor.
     *
     * @param t
     *            The transaction running the insert.
     * @param child
     *            The child operator from which to read tuples to be inserted.
     * @param tableId
     *            The table in which to insert tuples.
     * @throws DbException
     *             if TupleDesc of child differs from table into which we are to
     *             insert.
     */
    public Insert(TransactionId t,DbIterator child, int tableId)
            throws DbException {
        // some code goes here
        // check for incompatible tuple instance
        TupleDesc current = Database.getCatalog().getTupleDesc(tableId);
        if (!child.getTupleDesc().equals(current)){
            throw new DbException("incompatible tuples");
        }
        this.transId =t;
        this.dbItor=child;
        this.tableId = tableId;

        // assign td
        Type[] types = {Type.INT_TYPE};
        td = new TupleDesc(types);

    }

    public TupleDesc getTupleDesc() {
        // some code goes here
        return this.td;
    }

    public void open() throws DbException, TransactionAbortedException {
        // some code goes here
        dbItor.open();
        super.open();
    }

    public void close() {
        // some code goes here
        super.close();
        dbItor.close();
    }

    public void rewind() throws DbException, TransactionAbortedException {
        // some code goes here
        this.dbItor.rewind();
    }

    /**
     * Inserts tuples read from child into the tableId specified by the
     * constructor. It returns a one field tuple containing the number of
     * inserted records. Inserts should be passed through BufferPool. An
     * instances of BufferPool is available via Database.getBufferPool(). Note
     * that insert DOES NOT need check to see if a particular tuple is a
     * duplicate before inserting it.
     *
     * @return A 1-field tuple containing the number of inserted records, or
     *         null if called more than once.
     * @see Database#getBufferPool
     * @see BufferPool#insertTuple
     */
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        // some code goes here
        if (finished){
            return null;
        }
        int counter = 0;
        while(this.dbItor.hasNext()){
            Tuple t = dbItor.next();
            try {
                Database.getBufferPool().insertTuple(transId,tableId, t);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            counter ++;
        }
        //create a new tuple with num of counter to return
        Tuple result = new Tuple(td);
        result.setField(0, new IntField(counter));
        // need to set a flag as true so that we do not access multiple times
        finished = true;
        return result;
    }

    @Override
    public DbIterator[] getChildren() {
        // some code goes here
        return new DbIterator[]{this.dbItor};
    }

    @Override
    public void setChildren(DbIterator[] children) {
        // some code goes here
        this.dbItor = children[0];
    }
}
