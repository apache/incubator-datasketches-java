/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.datasketches.req;

import java.util.List;

/**
 * The implementation of the ReqDebug interface.
 * @author Lee Rhodes
 */
public class ReqDebugImpl implements ReqDebug{
  private static final String LS = System.getProperty("line.separator");
  private static final String TAB = "\t";
  private ReqSketch sk;
  private List<ReqCompactor> compactors;
  final int debugLevel;

  /**
   * Constructor
   * @param debugLevel sets the debug level of detail
   */
  public ReqDebugImpl(int debugLevel) {
    this.debugLevel = debugLevel;
  }

  @Override
  public void emitStart(ReqSketch sk) {
    if (debugLevel == 0) { return; }
    this.sk = sk;
    compactors = sk.getCompactors();
    println("START");
  }

  @Override
  public void emitStartCompress() {
    if (debugLevel == 0) { return; }
    int retItems = sk.getRetainedItems();
    int maxNomSize = sk.getMaxNomSize();
    long totalN = sk.getN();
    final StringBuilder sb = new StringBuilder();
    sb.append("COMPRESS: ");
    sb.append("skRetItems: ").append(retItems).append(" >= ");
    sb.append("MaxNomSize: ").append(maxNomSize);
    sb.append("  N: ").append(totalN);
    println(sb.toString());
    emitAllHorizList();
  }

  @Override
  public void emitCompressDone() {
    if (debugLevel == 0) { return; }
    int retItems = sk.getRetainedItems();
    int maxNomSize = sk.getMaxNomSize();
    emitAllHorizList();
    println("COMPRESS: DONE: SketchSize: " + retItems + TAB
        + " MaxNomSize: " + maxNomSize + LS + LS);
  }

  @Override
  public void emitAllHorizList() {
    if (debugLevel == 0) { return; }
    for (int h = 0; h < compactors.size(); h++) {
      final ReqCompactor c = compactors.get(h);
      print(c.toListPrefix());
      if (debugLevel > 1) {
        print(c.getBuffer().toHorizList("%4.0f", 20, 16) + LS);
      } else {
        print(LS);
      }
    }
  }

  @Override
  public void emitMustAddCompactor() {
    if (debugLevel == 0) { return; }
    int curLevels = sk.getNumLevels();
    ReqCompactor topC = compactors.get(curLevels - 1);
    int lgWt = topC.getLgWeight();
    int retCompItems = topC.getBuffer().getLength();
    int nomCap = topC.getNomCapacity();
    final StringBuilder sb = new StringBuilder();
    sb.append("  ");
    sb.append("Must Add Compactor: len(c[").append(lgWt).append("]): ");
    sb.append(retCompItems).append(" >= c[").append(lgWt).append("].nomCapacity(): ")
      .append(nomCap);
    println(sb.toString());
  }

  //compactor signals

  @Override
  public void emitCompactingStart(int lgWeight) {
    if (debugLevel == 0) { return; }
    ReqCompactor comp = compactors.get(lgWeight);
    int nomCap = comp.getNomCapacity();
    int secSize = comp.getSectionSize();
    int numSec = comp.getNumSections();
    int state = comp.getState();
    int bufCap = comp.getBuffer().getCapacity();  //TODO ?? do I want this or length
    final StringBuilder sb = new StringBuilder();
    sb.append(LS + "  ");
    sb.append("COMPACTING[").append(lgWeight).append("] ");
    sb.append("NomCapacity: ").append(nomCap);
    sb.append(TAB + " SectionSize: ").append(secSize);
    sb.append(TAB + " NumSections: ").append(numSec);
    sb.append(TAB + " State(bin): ").append(Integer.toBinaryString(state));
    sb.append(TAB + " BufCapacity: ").append(bufCap);
    println(sb.toString());
  }

  @Override
  public void emitNewCompactor(int lgWeight) {
    if (debugLevel == 0) { return; }
    ReqCompactor comp = compactors.get(lgWeight);
    println("    New Compactor: lgWeight: " + comp.getLgWeight()
        + TAB + "sectionSize: " + comp.getSectionSize()
        + TAB + "numSections: " + comp.getNumSections());
  }

  @Override
  public void emitAdjSecSizeNumSec(int lgWeight) {
    if (debugLevel == 0) { return; }
    ReqCompactor comp = compactors.get(lgWeight);
    int secSize = comp.getSectionSize();
    int numSec = comp.getNumSections();
    final StringBuilder sb = new StringBuilder();
    sb.append("    ");
    sb.append("Adjust: SectionSize: ").append(secSize);
    sb.append(" NumSections: ").append(numSec);
    println(sb.toString());
  }

  @Override
  public void emitCompactionDetail(int compactionStart, int compactionEnd,
      int secsToCompact, int promoteLen, boolean coin) {
    if (debugLevel == 0) { return; }
    final StringBuilder sb = new StringBuilder();
    sb.append("    ");
    sb.append("SecsToCompact: ").append(secsToCompact);
    sb.append(TAB + " CompactStart: ").append(compactionStart);
    sb.append(TAB + " CompactEnd: ").append(compactionEnd).append(LS);
    final int delete = compactionEnd - compactionStart;
    final String oddOrEven = (coin) ? "Odds" : "Evens";
    sb.append("    ");
    sb.append("Promote: ").append(promoteLen);
    sb.append(TAB + " Delete: ").append(delete);
    sb.append(TAB + " Choose: ").append(oddOrEven);
    println(sb.toString());
  }

  @Override
  public void emitCompactionDone(int lgWeight) {
    if (debugLevel == 0) { return; }
    ReqCompactor comp = compactors.get(lgWeight);
    int numCompactions = comp.getNumCompactions();
    println("  COMPACTING DONE: NumCompactions: " + numCompactions + LS);
  }

  static final void printf(final String format, final Object ...args) {
    System.out.printf(format, args);
  }

  static final void print(final Object o) { System.out.print(o.toString()); }

  static final void println(final Object o) { System.out.println(o.toString()); }

}