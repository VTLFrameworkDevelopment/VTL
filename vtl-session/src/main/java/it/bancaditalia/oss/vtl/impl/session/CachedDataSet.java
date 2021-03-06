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
package it.bancaditalia.oss.vtl.impl.session;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.bancaditalia.oss.vtl.impl.types.dataset.NamedDataSet;
import it.bancaditalia.oss.vtl.model.data.DataPoint;
import it.bancaditalia.oss.vtl.model.data.DataSet;
import it.bancaditalia.oss.vtl.session.VTLSession;

public class CachedDataSet extends NamedDataSet
{
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(CachedDataSet.class);
	private static final Map<VTLSession, Map<String, SoftReference<Queue<DataPoint>>>> SESSION_CACHES = new ConcurrentHashMap<>();
	private static final Map<VTLSession, Map<String, AtomicBoolean>> SESSION_STATUSES = new ConcurrentHashMap<>();

	private final Map<String, SoftReference<Queue<DataPoint>>> cache;
	private final Map<String, AtomicBoolean> statuses;

	public CachedDataSet(VTLSession session, String alias, DataSet delegate)
	{
		super(alias, delegate);
		
		cache = SESSION_CACHES.computeIfAbsent(session, s -> new ConcurrentHashMap<>());
		statuses = SESSION_STATUSES.computeIfAbsent(session, s -> new ConcurrentHashMap<>());
	}

	public CachedDataSet(VTLSession session, NamedDataSet delegate)
	{
		this(session, delegate.getAlias(), delegate.getDelegate());
	}

	@Override
	public Stream<DataPoint> stream()
	{
		AtomicBoolean isCompleted = statuses.computeIfAbsent(getAlias(), alias -> new AtomicBoolean(true));
		synchronized (isCompleted)
		{
			while (!isCompleted.get())
				try
				{
					isCompleted.wait();
				}
				catch (InterruptedException e)
				{
					Thread.currentThread().interrupt();
					return Stream.empty();
				}
	
			Queue<DataPoint> maybeCached = cache.computeIfAbsent(getAlias(), alias -> new SoftReference<>(null)).get();
			if (isCompleted.get() && maybeCached != null)
			{
				LOGGER.debug("Cache hit for {}.", getAlias());
				return maybeCached.stream();
			}
			
			LOGGER.debug("Cache miss for {}.", getAlias());
			Queue<DataPoint> newQueue = new ConcurrentLinkedQueue<>();
			isCompleted.set(false);
			return getDelegate().stream()
					.peek(newQueue::offer)
					.onClose(() -> { 
							synchronized (isCompleted)
							{
								LOGGER.debug("Caching of {} completed.", getAlias());
								cache.put(getAlias(), new SoftReference<>(newQueue));
								isCompleted.set(true);
								isCompleted.notifyAll();
							}
					});
		}
	}
}
