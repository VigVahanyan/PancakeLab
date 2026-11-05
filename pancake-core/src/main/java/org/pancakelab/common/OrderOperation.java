package org.pancakelab.common;

/**
 * Operations that can be applied to an order; which ones are legal depends on its status.
 */
public enum OrderOperation {
    ADD_PANCAKE,
    ADD_INGREDIENT,
    REMOVE_PANCAKES,
    COMPLETE,
    CANCEL,
    PREPARE,
    DELIVER
}
