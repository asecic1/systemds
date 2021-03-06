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

package org.tugraz.sysds.runtime.instructions.cp;

import org.tugraz.sysds.lops.Lop;
import org.tugraz.sysds.runtime.controlprogram.context.ExecutionContext;
import org.tugraz.sysds.runtime.matrix.data.FrameBlock;
import org.tugraz.sysds.runtime.matrix.operators.Operator;

public class UnaryFrameCPInstruction extends UnaryCPInstruction {
	protected UnaryFrameCPInstruction(Operator op, CPOperand in, CPOperand out, String opcode, String instr) {
		super(CPType.Unary, op, in, out, opcode, instr);
	}

	@Override
	public void processInstruction(ExecutionContext ec) {
		if(getOpcode().equals("typeOf")) {
			FrameBlock inBlock = ec.getFrameInput(input1.getName());
			FrameBlock retBlock = inBlock.getSchemaTypeOf();
			ec.releaseFrameInput(input1.getName());
			ec.setFrameOutput(output.getName(), retBlock);
		}
		else if(getOpcode().equals("detectSchema"))
		{
			FrameBlock inBlock = ec.getFrameInput(input1.getName());
			FrameBlock retBlock = inBlock.detectSchemaFromRow(Lop.SAMPLE_FRACTION);
			ec.releaseFrameInput(input1.getName());
			ec.setFrameOutput(output.getName(), retBlock);
		}
	}
}