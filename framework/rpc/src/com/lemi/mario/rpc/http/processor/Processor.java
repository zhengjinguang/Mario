package com.lemi.mario.rpc.http.processor;


/**
 * This class defines a processor which can process input to output format.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 * 
 * @param <U> input type
 * @param <V> output type
 * @param <E> exception type
 */
public interface Processor<U, V, E extends Exception> {
  /**
   * Processes input.
   * 
   * @param input input
   * @return result
   * @throws E exception
   */
  V process(U input) throws E;
}
