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

import static java.util.stream.Collectors.toSet;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import it.bancaditalia.oss.vtl.exceptions.VTLMissingComponentsException;
import it.bancaditalia.oss.vtl.model.data.ComponentRole.Identifier;
import it.bancaditalia.oss.vtl.model.data.ComponentRole.Measure;
import it.bancaditalia.oss.vtl.model.domain.StringCodeListDomain;
import it.bancaditalia.oss.vtl.model.domain.StringDomain;

public interface DataStructure extends Set<DataStructureComponent<?, ?, ?>>, Serializable
{
	public <T extends ComponentRole> Set<DataStructureComponent<T, ?, ?>> getComponents(Class<T> typeOfComponent);

	public default <R extends ComponentRole, S extends ValueDomainSubset<D>, D extends ValueDomain> Set<DataStructureComponent<R, S, D>> getComponents(Class<R> role, S domain)
	{
		return getComponents(role).stream()
				.filter(c -> domain.isAssignableFrom(c.getDomain()))
				.map(c -> c.as(role).as(domain))
				.collect(toSet());
	}

	public DataStructure swapComponent(DataStructureComponent<?, ?, ?> oldComponent, DataStructureComponent<?, ?, ?> newComponent);

	public boolean contains(String component);

	public Optional<DataStructureComponent<?, ?, ?>> getComponent(String name);

	public default Set<DataStructureComponent<?, ?, ?>> getComponents(String... names)
	{
		return Arrays.stream(names)
				.map(this::getComponent)
				.map(o -> o.orElseThrow(() -> new VTLMissingComponentsException(this, names)))
				.collect(Collectors.toSet());
	}

	public default Set<DataStructureComponent<?, ?, ?>> getComponents(Collection<String> names)
	{
		return getComponents(names.toArray(new String[0]));
	}

	public default <R extends ComponentRole> Set<DataStructureComponent<R, ?, ?>> getComponents(Collection<String> names, Class<R> role)
	{
		return getComponents(names.toArray(new String[0])).stream()
				.filter(c -> c.is(role))
				.map(c -> c.as(role))
				.collect(Collectors.toSet());
	}

	public default <S extends ValueDomainSubset<D>, D extends ValueDomain> DataStructureComponent<?, S, D> getComponent(String name, S domain)
	{
		Optional<DataStructureComponent<?, S, D>> component = getComponent(name)
				.filter(c -> domain.isAssignableFrom(c.getDomain()))
				.map(c -> c.as(domain));
			
		return component.orElse(null);
	}

	public default <R extends ComponentRole> DataStructureComponent<R, ?, ?> getComponent(String name, Class<R> role)
	{
		Optional<DataStructureComponent<R, ?, ?>> component = getComponent(name)
				.filter(c -> c.is(role))
				.map(c -> c.as(role));
			
		return component.orElse(null);
	}

	public default <R extends ComponentRole, S extends ValueDomainSubset<D>, D extends ValueDomain> DataStructureComponent<R, S, D> getComponent(String name, Class<R> role, S domain)
	{
		Optional<DataStructureComponent<R, S, D>> component = getComponent(name)
				.filter(c -> domain.isAssignableFrom(c.getDomain()))
				.filter(c -> c.is(role))
				.map(c -> c.as(domain))
				.map(c -> c.as(role));
		
		return component.orElse(null);
	}

	public DataStructure subspace(Collection<? extends DataStructureComponent<Identifier, ?, ?>> subspace);
	
	public DataStructure keep(String... names);
	
	public DataStructure drop(Collection<String> names);

	public DataStructure membership(String name);

	public DataStructure joinForOperators(DataStructure other);

	public DataStructure rename(DataStructureComponent<?, ?, ?> component, String newName);

	public <S extends ValueDomainSubset<D>, D extends ValueDomain> DataStructure pivot(DataStructureComponent<Identifier, StringCodeListDomain, StringDomain> identifier, DataStructureComponent<Measure, S, D> measure);

	public boolean containsComponent(String componentName);
}
