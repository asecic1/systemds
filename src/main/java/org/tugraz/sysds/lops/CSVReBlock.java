/*
 * Modifications Copyright 2019 Graz University of Technology
 *
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
 
package org.tugraz.sysds.lops;

import org.tugraz.sysds.lops.LopProperties.ExecType;
import org.tugraz.sysds.parser.DataExpression;
import org.tugraz.sysds.runtime.instructions.InstructionUtils;
import org.tugraz.sysds.common.Types.DataType;
import org.tugraz.sysds.common.Types.ValueType;


/**
 * Lop to convert CSV data into SystemDS data format (TextCell, BinaryCell, or BinaryBlock)
 */
public class CSVReBlock extends Lop 
{
	public static final String OPCODE = "csvrblk"; 
	
	private int _blocksize;

	public CSVReBlock(Lop input, int blen, DataType dt, ValueType vt, ExecType et)
	{
		super(Lop.Type.CSVReBlock, dt, vt);
		addInput(input);
		input.addOutput(this);
		
		_blocksize = blen;
		
		if(et == ExecType.SPARK) {
			lps.setProperties( inputs, ExecType.SPARK);
		}
		else {
			throw new LopsException("Incorrect execution type for CSVReblock:" + et);
		}
	}

	@Override
	public String toString() {
		return "CSVReblock - blocksize = " + _blocksize;
	}
	
	private String prepCSVProperties() {
		StringBuilder sb = new StringBuilder();

		Data dataInput = (Data)getInputs().get(0);
		
		Lop headerLop = dataInput.getNamedInputLop(DataExpression.DELIM_HAS_HEADER_ROW, 
			String.valueOf(DataExpression.DEFAULT_DELIM_HAS_HEADER_ROW));
		Lop delimLop = dataInput.getNamedInputLop(DataExpression.DELIM_DELIMITER, 
			DataExpression.DEFAULT_DELIM_DELIMITER);
		Lop fillLop = dataInput.getNamedInputLop(DataExpression.DELIM_FILL, 
			String.valueOf(DataExpression.DEFAULT_DELIM_FILL)); 
		Lop fillValueLop = dataInput.getNamedInputLop(DataExpression.DELIM_FILL_VALUE, 
			String.valueOf(DataExpression.DEFAULT_DELIM_FILL_VALUE));
		
		if (headerLop.isVariable())
			throw new LopsException(this.printErrorLocation()
					+ "Parameter " + DataExpression.DELIM_HAS_HEADER_ROW
					+ " must be a literal.");
		if (delimLop.isVariable())
			throw new LopsException(this.printErrorLocation()
					+ "Parameter " + DataExpression.DELIM_DELIMITER
					+ " must be a literal.");
		if (fillLop.isVariable())
			throw new LopsException(this.printErrorLocation()
					+ "Parameter " + DataExpression.DELIM_FILL
					+ " must be a literal.");
		if (fillValueLop.isVariable())
			throw new LopsException(this.printErrorLocation()
					+ "Parameter " + DataExpression.DELIM_FILL_VALUE
					+ " must be a literal.");

		sb.append( ((Data)headerLop).getBooleanValue() );
		sb.append( OPERAND_DELIMITOR );
		sb.append( ((Data)delimLop).getStringValue() );
		sb.append( OPERAND_DELIMITOR );
		sb.append( ((Data)fillLop).getBooleanValue() );
		sb.append( OPERAND_DELIMITOR );
		sb.append( ((Data)fillValueLop).getDoubleValue() );
		
		return sb.toString();
	}

	@Override
	public String getInstructions(String input1, String output) {
		return InstructionUtils.concatOperands(
			getExecType().name(),
			OPCODE,
			getInputs().get(0).prepInputOperand(input1),
			prepOutputOperand(output),
			String.valueOf(_blocksize),
			prepCSVProperties());
	}
}
