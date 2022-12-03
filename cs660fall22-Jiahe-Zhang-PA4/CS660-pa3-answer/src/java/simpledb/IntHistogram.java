package simpledb;

import java.util.ArrayList;

/** A class to represent a fixed-width histogram over a single integer-based field.
 */
public class IntHistogram {
    private final int num_buckets;
    private final int min;
    private final int max;
    private final int[] buckets;
    private final double avgSelectivity;
    private int totalNumber;
    private final int mod;
    /**
     * Create a new IntHistogram.
     * 
     * This IntHistogram should maintain a histogram of integer values that it receives.
     * It should split the histogram into "buckets" buckets.
     * 
     * The values that are being histogrammed will be provided one-at-a-time through the "addValue()" function.
     * 
     * Your implementation should use space and have execution time that are both
     * constant with respect to the number of values being histogrammed.  For example, you shouldn't 
     * simply store every value that you see in a sorted list.
     * 
     * @param buckets The number of buckets to split the input value into.
     * @param min The minimum integer value that will ever be passed to this class for histogramming
     * @param max The maximum integer value that will ever be passed to this class for histogramming
     */
    public IntHistogram(int buckets, int min, int max) {
    	// some code goes here
        this.num_buckets = buckets;
        this.buckets = new int[buckets];
        this.min = min;
        this.max = max;
        this.avgSelectivity = 0.0;
        this.totalNumber = 0;
        this.mod = (int) Math.ceil((double) (max - min + 1)/num_buckets);
    }

    /**
     * Add a value to the set of values that you are keeping a histogram of.
     * @param v Value to add to the histogram
     */
    public void addValue(int v) {
    	// some code goes here
        int bucket = (v - this.min)/this.mod;
        this.buckets[bucket]++;
        this.totalNumber++;
    }

    /**
     * Estimate the selectivity of a particular predicate and operand on this table.
     * 
     * For example, if "op" is "GREATER_THAN" and "v" is 5, 
     * return your estimate of the fraction of elements that are greater than 5.
     * 
     * @param op Operator
     * @param v Value
     * @return Predicted selectivity of this particular operator and value
     */
    public double estimateSelectivity(Predicate.Op op, int v) {

    	// some code goes here
        int bucket = (v - this.min)/this.mod;
        // the cases are different operators, they are GREATER_THAN_OR_EQ, GREATER_THAN
        switch (op) {
            case EQUALS: 
            case LIKE:
                return setBuckets(bucket);
            case GREATER_THAN:
            case LESS_THAN:
                return setBucketsNeq(bucket, op, v);
            case GREATER_THAN_OR_EQ:
                return setBuckets(bucket) + setBucketsNeq(bucket, Predicate.Op.GREATER_THAN, v);
            case LESS_THAN_OR_EQ:
                return setBuckets(bucket) + setBucketsNeq(bucket, Predicate.Op.LESS_THAN, v);
            case NOT_EQUALS:
                return 1.0 - setBuckets(bucket);
            default:
                return -1.0;
        }
    }
    private double setBuckets(int bucket){
        if (bucket < 0 || bucket >= this.num_buckets)
            return 0.0;
        return (double) ((double) this.buckets[bucket]/ this.mod)/this.totalNumber;
    }
    private double setBucketsNeq(int bucket, Predicate.Op op, int v){
//        int bucket = (v - this.min)/this.mod;

        int right, left, height, f;
        if (bucket < 0){
            right = 0;
            left = -1;
            f = 0;
            height = 0;}
        else if (bucket >= this.num_buckets){
            right = this.num_buckets;
            left = this.num_buckets-1;
            f =0;
            height =0;

        }else {
            right = bucket + 1;
            left = bucket -1;
            f = -1;
            height = this.buckets[bucket];
        }
        double resultSelectivity = 0.0;

        if(op == Predicate.Op.GREATER_THAN) {

                if (f == -1){
                    f = ((right*mod)+this.min-v)/mod;
                }
                resultSelectivity = (double) (height * f) / this.totalNumber;
                if (right >= this.num_buckets)
                    return resultSelectivity/this.totalNumber;
                for (int i = right; i < this.num_buckets; i++)
                {
                    resultSelectivity += this.buckets[i];
                }
                return resultSelectivity/this.totalNumber;}
        else if (op == Predicate.Op.LESS_THAN){if (f == -1){
            f = (v-(left*mod)+this.min)/mod;
        }
            resultSelectivity =(double) (height * f) / this.totalNumber;
            if (left < 0)
                return resultSelectivity/this.totalNumber;
            for (int i = left; i >= 0; i--)
            {
                resultSelectivity += this.buckets[i];
            }
            return resultSelectivity/this.totalNumber;}
        else{
            return -1.0;
        }

    }
    /**
     * @return
     *     the average selectivity of this histogram.
     *     
     *     This is not an indispensable method to implement the basic
     *     join optimization. It may be needed if you want to
     *     implement a more efficient optimization
     * */
    public double avgSelectivity()
    {
        // some code goes here
        return avgSelectivity;
    }
    
    /**
     * @return A string describing this histogram, for debugging purposes
     */
    public String toString() {
        // some code goes here
        String result = "";
        for (int i =0; i< this.num_buckets; i ++){
            result += i + "bucket: ";
            for (int j  = 0; j < this.buckets[i]; j++){
                result += "|";
            }
            result += '\n';
        }
        return result;
    }
}
