/*******************************************************************************
 * Copyright 2020, Bank Of Italy
 *
 * Licensed under the EUPL, Version 1.2 (the "License");
 * You may not use this work except in compliance with the
 * License.
 * You may obtain a copy of the License at:
 *
 * https://joinup.ec.europa.eu/sites/default/files/custom-page/attachment/2020-03/EUPL-1.2%20EN.txt
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the License is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 *
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *******************************************************************************/
package it.bancaditalia.oss.vtl.impl.transform.clause;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import it.bancaditalia.oss.vtl.exceptions.VTLMissingComponentsException;
import it.bancaditalia.oss.vtl.impl.transform.exceptions.VTLInvalidParameterException;
import it.bancaditalia.oss.vtl.impl.types.exceptions.VTLInvariantIdentifiersException;
import it.bancaditalia.oss.vtl.model.data.ComponentRole.Identifier;
import it.bancaditalia.oss.vtl.model.data.ComponentRole.NonIdentifier;
import it.bancaditalia.oss.vtl.model.data.DataSet;
import it.bancaditalia.oss.vtl.model.data.DataSet.VTLDataSetMetadata;
import it.bancaditalia.oss.vtl.model.data.DataStructure;
import it.bancaditalia.oss.vtl.model.data.DataStructureComponent;
import it.bancaditalia.oss.vtl.model.data.ScalarValue;
import it.bancaditalia.oss.vtl.model.data.VTLValue;
import it.bancaditalia.oss.vtl.model.data.VTLValue.VTLValueMetadata;
import it.bancaditalia.oss.vtl.model.transform.TransformationScheme;
import it.bancaditalia.oss.vtl.util.Utils;

public class DropClauseTransformation extends DatasetClauseTransformation
{
	private static final long serialVersionUID = 1L;

	private final List<String> names;
	private VTLDataSetMetadata metadata;
	
	public DropClauseTransformation(List<String> names)
	{
		this.names = names;
	}

	@Override
	public VTLValue eval(TransformationScheme session)
	{
		DataSet dataset = (DataSet) getThisValue(session);
		Set<DataStructureComponent<NonIdentifier, ?, ?>> toDrop = Utils.getStream(names)
				.map(n -> dataset.getComponent(n))
				.map(Optional::get)
				.map(c -> c.as(NonIdentifier.class))
				.collect(toSet());
		
		return dataset.mapKeepingKeys(metadata, dp -> {
				Map<DataStructureComponent<? extends NonIdentifier, ?, ?>, ScalarValue<?, ?, ?>> newVals = new HashMap<>(dp.getValues(NonIdentifier.class));
				newVals.keySet().removeAll(toDrop);
				return newVals;
			});
	}
	
	@Override
	public VTLDataSetMetadata getMetadata(TransformationScheme session)
	{
		VTLValueMetadata operand = getThisMetadata(session);
		
		if (!(operand instanceof VTLDataSetMetadata))
			throw new VTLInvalidParameterException(operand, VTLDataSetMetadata.class);
		
		Set<? extends DataStructureComponent<? extends Identifier, ?, ?>> namedIDs = Utils.getStream(names)
				.map(((VTLDataSetMetadata)operand)::getComponent)
				.map(o -> o.orElseThrow(() -> new VTLMissingComponentsException((DataStructure) operand, names.toArray(new String[0]))))
				.filter(c -> c.is(Identifier.class))
				.map(c -> c.as(Identifier.class))
				.collect(toSet());
		
		if (!namedIDs.isEmpty())
			throw new VTLInvariantIdentifiersException("drop", namedIDs);

		metadata = ((VTLDataSetMetadata) operand).drop(names);
		
		return metadata;
	}

	@Override
	public String toString()
	{
		return names.stream().collect(joining(", ", "[drop ", "]"));
	}
}
