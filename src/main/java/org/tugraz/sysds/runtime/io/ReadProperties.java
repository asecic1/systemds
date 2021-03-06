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

package org.tugraz.sysds.runtime.io;

import org.tugraz.sysds.runtime.matrix.data.InputInfo;

public class ReadProperties 
{
	// Properties common to all file formats 
	public String path;
	public long rlen, clen;
	public int blen;
	public long expectedNnz;
	public InputInfo inputInfo;
	public boolean localFS;
	
	// Properties specific to CSV files
	public FileFormatProperties formatProperties;
	
	public ReadProperties() {
		rlen = -1;
		clen = -1;
		blen = -1;
		expectedNnz = -1;
		inputInfo = null;
		localFS = false;
	}
}
