package com.project.nicki.displaystabilizer.dataprocessor.utils;

/**
 * Created by nickisverygood on 1/1/2016.
 */
// LMfunc.java
/**
 * Caller implement this interface to specify the
 * function to be minimized and its gradient.
 *
 * Optionally return an initial guess and some test buffer,
 * though the LM.java only uses this in its optional main() test program.
 * Return null if these are not needed.
 */
public interface LMfunc
{

    /**
     * x is a single point, but domain may be mulidimensional
     */
    double val(double[] x, double[] a);

    /**
     * return the kth component of the gradient df(x,a)/da_k
     */
    double grad(double[] x, double[] a, int ak);

    /**
     * return initial guess at a[]
     */
    double[] initial();

    /**
     * return an array[4] of x,a,y,s for a test case;
     * a is the desired final answer.
     */
    Object[] testdata();

} //LMfunc

