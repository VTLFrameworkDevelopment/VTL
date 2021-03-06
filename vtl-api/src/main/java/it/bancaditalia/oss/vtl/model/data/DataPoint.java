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
package it.bancaditalia.oss.vtl.model.data;

import static it.bancaditalia.oss.vtl.util.Utils.byKey;
import static it.bancaditalia.oss.vtl.util.Utils.byKeyValue;
import static it.bancaditalia.oss.vtl.util.Utils.byValue;
import static it.bancaditalia.oss.vtl.util.Utils.entriesToMap;
import static it.bancaditalia.oss.vtl.util.Utils.keepingValue;
import static it.bancaditalia.oss.vtl.util.Utils.toMapWithValues;

import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import it.bancaditalia.oss.vtl.model.data.ComponentRole.Identifier;
import it.bancaditalia.oss.vtl.model.data.ComponentRole.Measure;
import it.bancaditalia.oss.vtl.util.Utils;

public interface DataPoint extends Map<DataStructureComponent<?, ?, ?>, ScalarValue<?, ?, ?>>, Serializable
{
	public DataPoint dropComponents(Set<DataStructureComponent<?, ?, ?>> components);

	public DataPoint merge(DataPoint other);

	public DataPoint keep(Collection<? extends DataStructureComponent<?, ?, ?>> components);

	public DataPoint renameComponent(DataStructureComponent<?, ?, ?> oldComponent, DataStructureComponent<?, ?, ?> newComponent);

	public DataPoint alter(Map<? extends DataStructureComponent<Measure, ?, ?>, ? extends ScalarValue<?, ?, ?>> measures);

	@SuppressWarnings("unchecked")
	public default <S extends ValueDomainSubset<D>, D extends ValueDomain> ScalarValue<?, S, D> getValue(DataStructureComponent<?, S, D> component)
	{
		return (ScalarValue<?, S, D>) component.getDomain().cast(get(component));
	}
	
	public default <S extends ValueDomainSubset<D>, D extends ValueDomain, SV extends S, SD extends D> DataPoint alter(DataStructureComponent<Measure, S, D> measure, ScalarValue<?, ? extends S, D> value)
	{
		return alter(Collections.singletonMap(measure, value));
	}

	public DataPoint subspace(Set<? extends DataStructureComponent<Identifier, ?, ?>> subspace);

	public DataPoint replaceComponent(DataStructureComponent<?, ?, ?> component, ScalarValue<?, ?, ?> value);
	
	public default boolean matches(Map<? extends DataStructureComponent<? extends Identifier, ?, ?>, ? extends ScalarValue<?, ?, ?>> identifierValues)
	{
		return !Utils.getStream(identifierValues.entrySet())
				.filter(byKeyValue((k, v) -> !get(k).equals(k.cast(v))))
				.findAny()
				.isPresent();
	}

	public <R extends ComponentRole> Map<DataStructureComponent<R, ?, ?>, ScalarValue<?, ?, ?>> getValues(Class<R> role);

	public default Map<DataStructureComponent<?, ?, ?>, ScalarValue<?, ?, ?>> getValues(Set<DataStructureComponent<?, ?, ?>> components)
	{
		return Utils.getStream(components)
				.filter(this::containsKey)
				.collect(toMapWithValues(this::get));
	}

	public default <R extends ComponentRole> Map<DataStructureComponent<R, ?, ?>, ScalarValue<?, ?, ?>> getValues(Class<R> role, Collection<String> components)
	{
		return Utils.getStream(keySet())
				.map(c -> new SimpleEntry<>(c, c.getName()))
				.filter(byValue(components::contains))
				.map(Entry::getKey)
				.filter(c -> c.is(role))
				.map(c -> c.as(role))
				.collect(toMapWithValues(this::get));
	}

	public default <R extends ComponentRole> Map<DataStructureComponent<R, ?, ?>, ScalarValue<?, ?, ?>> getValues(Set<DataStructureComponent<R, ?, ?>> components, Class<R> role)
	{
		Stream<Entry<DataStructureComponent<R, ?, ?>, ScalarValue<?, ?, ?>>> stream = Utils.getStream(getValues(role).entrySet())
				.filter(byKey(components::contains))
				.map(keepingValue(c -> c.as(role)));
		return stream.collect(entriesToMap());
	}
}
