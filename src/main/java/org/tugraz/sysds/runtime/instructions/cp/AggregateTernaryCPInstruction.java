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

package org.tugraz.sysds.runtime.instructions.cp;

import org.tugraz.sysds.runtime.DMLRuntimeException;
import org.tugraz.sysds.runtime.controlprogram.context.ExecutionContext;
import org.tugraz.sysds.runtime.instructions.InstructionUtils;
import org.tugraz.sysds.runtime.matrix.data.MatrixBlock;
import org.tugraz.sysds.runtime.matrix.operators.AggregateTernaryOperator;
import org.tugraz.sysds.runtime.matrix.operators.Operator;

public class AggregateTernaryCPInstruction extends ComputationCPInstruction {

	private AggregateTernaryCPInstruction(Operator op, CPOperand in1, CPOperand in2, CPOperand in3, CPOperand out,
			String opcode, String istr) {
		super(CPType.AggregateTernary, op, in1, in2, in3, out, opcode, istr);
	}

	public static AggregateTernaryCPInstruction parseInstruction( String str ) {
		String[] parts = InstructionUtils.getInstructionPartsWithValueType(str);
		String opcode = parts[0];
		
		if ( opcode.equalsIgnoreCase("tak+*") || opcode.equalsIgnoreCase("tack+*") ) {
			InstructionUtils.checkNumFields( parts, 5 );
			
			CPOperand in1 = new CPOperand(parts[1]);
			CPOperand in2 = new CPOperand(parts[2]);
			CPOperand in3 = new CPOperand(parts[3]);
			CPOperand out = new CPOperand(parts[4]);
			int numThreads = Integer.parseInt(parts[5]);
			
			AggregateTernaryOperator op = InstructionUtils.parseAggregateTernaryOperator(opcode, numThreads);
			return new AggregateTernaryCPInstruction(op, in1, in2, in3, out, opcode, str);
		} 
		else {
			throw new DMLRuntimeException("AggregateTernaryInstruction.parseInstruction():: Unknown opcode " + opcode);
		}		
	}
	
	@Override
	public void processInstruction(ExecutionContext ec) {
		MatrixBlock matBlock1 = ec.getMatrixInput(input1.getName());
		MatrixBlock matBlock2 = ec.getMatrixInput(input2.getName());
		MatrixBlock matBlock3 = input3.isLiteral() ? null : //matrix or literal 1
			ec.getMatrixInput(input3.getName());
		
		AggregateTernaryOperator ab_op = (AggregateTernaryOperator) _optr;
		MatrixBlock ret = matBlock1.aggregateTernaryOperations(
				matBlock1, matBlock2, matBlock3, new MatrixBlock(), ab_op, true);
		
		//release inputs/outputs
		ec.releaseMatrixInput(input1.getName());
		ec.releaseMatrixInput(input2.getName());
		if( !input3.isLiteral() )
			ec.releaseMatrixInput(input3.getName());
		if( output.getDataType().isScalar() )
			ec.setScalarOutput(output.getName(), new DoubleObject(ret.quickGetValue(0, 0)));
		else
			ec.setMatrixOutput(output.getName(), ret);
	}
}
