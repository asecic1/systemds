/*
 * Copyright 2019 Graz University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.tugraz.sysds.runtime.instructions.fed;

import org.tugraz.sysds.api.DMLScript;
import org.tugraz.sysds.common.Types.DataType;
import org.tugraz.sysds.lops.Lop;
import org.tugraz.sysds.runtime.DMLRuntimeException;
import org.tugraz.sysds.runtime.controlprogram.LocalVariableMap;
import org.tugraz.sysds.runtime.controlprogram.caching.MatrixObject;
import org.tugraz.sysds.runtime.controlprogram.context.ExecutionContext;
import org.tugraz.sysds.runtime.instructions.CPInstructionParser;
import org.tugraz.sysds.runtime.instructions.Instruction;
import org.tugraz.sysds.runtime.instructions.cp.Data;
import org.tugraz.sysds.runtime.instructions.cp.ScalarObject;
import org.tugraz.sysds.runtime.matrix.operators.Operator;

public abstract class FEDInstruction extends Instruction
{
	public enum FEDType {
		Init
	}

	protected final FEDType _fedType;
	protected final Operator _optr;
	protected final boolean _requiresLabelUpdate;

	protected FEDInstruction(FEDType type, String opcode, String istr) {
		this(type, null, opcode, istr);
	}

	protected FEDInstruction(FEDType type, Operator op, String opcode, String istr) {
		_fedType = type;
		_optr = op;
		instString = istr;

		// prepare opcode and update requirement for repeated usage
		instOpcode = opcode;
		_requiresLabelUpdate = super.requiresLabelUpdate();
	}
	
	@Override
	public IType getType() {
		return IType.FEDERATED;
	}

	public FEDType getFEDInstructionType() {
		return _fedType;
	}
	
	@Override
	public boolean requiresLabelUpdate() {
		return _requiresLabelUpdate;
	}

	@Override
	public String getGraphString() {
		return getOpcode();
	}

	@Override
	public Instruction preprocessInstruction(ExecutionContext ec) {
		//default preprocess behavior (e.g., debug state)
		Instruction tmp = super.preprocessInstruction(ec);

		//instruction patching
		if( tmp.requiresLabelUpdate() ) { //update labels only if required
			//note: no exchange of updated instruction as labels might change in the general case
			String updInst = updateLabels(tmp.toString(), ec.getVariables());
			tmp = CPInstructionParser.parseSingleInstruction(updInst);
			// Corrected lineage trace for patched instructions
			if (DMLScript.LINEAGE)
				ec.traceLineage(tmp);
		}
		return tmp;
	}

	@Override 
	public abstract void processInstruction(ExecutionContext ec);
	
	/**
	 * Takes a delimited string of instructions, and replaces ALL placeholder labels 
	 * (such as ##mVar2## and ##Var5##) in ALL instructions.
	 *  
	 * @param instList instruction list as string
	 * @param labelValueMapping local variable map
	 * @return instruction list after replacement
	 */
	public static String updateLabels (String instList, LocalVariableMap labelValueMapping) {
		if ( !instList.contains(Lop.VARIABLE_NAME_PLACEHOLDER) )
			return instList;
		
		StringBuilder updateInstList = new StringBuilder();
		String[] ilist = instList.split(Lop.INSTRUCTION_DELIMITOR); 
		
		for ( int i=0; i < ilist.length; i++ ) {
			if ( i > 0 )
				updateInstList.append(Lop.INSTRUCTION_DELIMITOR);
			
			updateInstList.append( updateInstLabels(ilist[i], labelValueMapping));
		}
		return updateInstList.toString();
	}
	
	/** 
	 * Replaces ALL placeholder strings (such as ##mVar2## and ##Var5##) in a single instruction.
	 *  
	 * @param inst string instruction
	 * @param map local variable map
	 * @return string instruction after replacement
	 */
	private static String updateInstLabels(String inst, LocalVariableMap map) {
		if ( inst.contains(Lop.VARIABLE_NAME_PLACEHOLDER) ) {
			int skip = Lop.VARIABLE_NAME_PLACEHOLDER.length();
			while ( inst.contains(Lop.VARIABLE_NAME_PLACEHOLDER) ) {
				int startLoc = inst.indexOf(Lop.VARIABLE_NAME_PLACEHOLDER)+skip;
				String varName = inst.substring(startLoc, inst.indexOf(Lop.VARIABLE_NAME_PLACEHOLDER, startLoc));
				String replacement = getVarNameReplacement(inst, varName, map);
				inst = inst.replaceAll(Lop.VARIABLE_NAME_PLACEHOLDER + varName + Lop.VARIABLE_NAME_PLACEHOLDER, replacement);
			}
		}
		return inst;
	}
	
	/**
	 * Computes the replacement string for a given variable name placeholder string 
	 * (e.g., ##mVar2## or ##Var5##). The replacement is a HDFS filename for matrix 
	 * variables, and is the actual value (stored in symbol table) for scalar variables.
	 * 
	 * @param inst instruction
	 * @param varName variable name
	 * @param map local variable map
	 * @return string variable name
	 */
	private static String getVarNameReplacement(String inst, String varName, LocalVariableMap map) {
		Data val = map.get(varName);
		if (val != null) {
			String replacement = null;
			if (val.getDataType() == DataType.MATRIX) {
				replacement = ((MatrixObject) val).getFileName();
			}

			if (val.getDataType() == DataType.SCALAR)
				replacement = "" + ((ScalarObject) val).getStringValue();
			return replacement;
		} else {
			throw new DMLRuntimeException("Variable (" + varName + ") in Instruction (" + inst + ") is not found in the variablemap.");
		}
	}
}
