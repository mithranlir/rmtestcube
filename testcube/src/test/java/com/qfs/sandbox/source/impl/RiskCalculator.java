/*
 * (C) Quartet FS 2007-2013
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.qfs.sandbox.source.impl;

import com.qfs.chunk.IArrayReader;
import com.qfs.chunk.IWritableArray;
import com.qfs.store.record.IRecordFormat;
import com.qfs.store.transaction.ITransactionManager.IUpdateWhereProcedure;
import jsr166e.ThreadLocalRandom;

import java.util.Random;


public class RiskCalculator implements IUpdateWhereProcedure {

	protected int pnlIndex;

	protected RiskCalculator(){} // Serialization

	public RiskCalculator(int pnlIndex)  {
		this.pnlIndex = pnlIndex;
	}

	@Override
	public void init(IRecordFormat selectionFormat) {
	}

	@Override
	public void execute(IArrayReader selectedRecord, IWritableArray recordWriter) {
		final Random random = ThreadLocalRandom.current();
		recordWriter.writeDouble(this.pnlIndex, random.nextInt(1000) * 10.3d);
	}


}
