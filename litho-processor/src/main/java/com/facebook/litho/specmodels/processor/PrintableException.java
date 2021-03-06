/*
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho.specmodels.processor;

import javax.annotation.processing.Messager;

public abstract class PrintableException extends RuntimeException {

  PrintableException() {
    super();
  }

  PrintableException(String message) {
    super(message);
  }

  public abstract void print(Messager messager);
}
