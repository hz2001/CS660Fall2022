package simpledb;

import java.io.IOException;

/**
 * The delete operator. Delete reads tuples from its child operator and removes
 * them from the table they belong to.
 */
public class Delete extends Operator {

    private static final long serialVersionUID = 1L;
    private final TransactionId transId;
    private DbIterator dbItor;

    private final TupleDesc td;
    private  boolean finished = false;

    /**
     * Constructor specifying the transaction that this delete belongs to as
     * well as the child to read from.
     * 
     * @param t
     *            The transaction this delete runs in
     * @param child
     *            The child operator from which to read tuples for deletion
     */
    public Delete(TransactionId t, DbIterator child) {
        // some code goes here
        this.transId =t;
        this.dbItor=child;

        // assign td
        Type[] types = {Type.INT_TYPE};
        td = new TupleDesc(types);
    }

    public TupleDesc getTupleDesc() {
        // some code goes here
        return this.dbItor.getTupleDesc();
    }

    public void open() throws DbException, TransactionAbortedException {
        // some code goes here
        this.dbItor.open();
        super.open();
    }

    public void close() {
        // some code goes here
        super.close();
        this.dbItor.close();
    }

    public void rewind() throws DbException, TransactionAbortedException {
        // some code goes here
        this.dbItor.rewind();
    }

    /**
     * Deletes tuples as they are read from the child operator. Deletes are
     * processed via the buffer pool (which can be accessed via the
     * Database.getBufferPool() method.
     * 
     * @return A 1-field tuple containing the number of deleted records.
     * @see Database#getBufferPool
     * @see BufferPool#deleteTuple
     */
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        // some code goes here
        // same as insert
        if (finished){
            return null;
        }
        int counter = 0;
        while(this.dbItor.hasNext()){
            Tuple t = dbItor.next();
            try {
                Database.getBufferPool().deleteTuple(transId, t);
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
