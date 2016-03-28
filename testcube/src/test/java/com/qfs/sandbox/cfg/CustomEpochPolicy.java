/*
 * (C) Quartet FS 2014
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.qfs.sandbox.cfg;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import com.qfs.fwk.ordering.impl.EpochComparator;
import com.qfs.multiversion.ADefaultEpochPolicy;
import com.qfs.multiversion.IEpoch;
import com.qfs.multiversion.IEpochHistory;
import com.qfs.multiversion.IEpochManagementPolicy;
import com.qfs.multiversion.IVersionHistory;
import com.quartetfs.fwk.util.IPredicate;

/**
 * A custom epoch management {@link IEpochManagementPolicy policy} that keeps a version
 * each fixed period of time and releases all the others.
 * <p>
 * Example: <code> CustomEpochPolicy(2 * 60_000, 5 * 60_000, 30 * 60_000) </code>
 * Keeps the last 2 minutes, then one version each 5 minutes until the last half an hour.
 *
 * @author Quartet FS
 */
public class CustomEpochPolicy extends ADefaultEpochPolicy {

	/** The minimal time period (in ms), a version should be kept in the history */
	protected final long minTimeToKeep;

	/** The interval of time (in ms), after each, we should keep a version */
	protected final long period;

	/** The maximal time period (in ms), a version can be kept.*/
	protected final long maxTimeToKeep;

	/** For each component, versions that are periodically kept (rule 2) */
	protected final ConcurrentHashMap<String, SortedSet<IEpoch>> periodicKeptById = new ConcurrentHashMap<>();

	/**
	 * Constructor of a {@link IEpochManagementPolicy} that executes the 3 following rules:
	 * <ul>
	 * <li> Rule 1: Keeps all versions created in the last {@code minTimeToKeep} milliseconds.
	 * <li> Rule 2: Keeps one version each {@code period} milliseconds until {@code maxTimeToKeep} milliseconds.
	 * <li> Rule 3: Releases all other versions.
	 * </ul>
	 *
	 * @param minTimeToKeep The MINIMAL time period (in ms), a version should be kept in the history.
	 * @param period The interval of time (in ms), after each, we should keep a version.
	 * @param maxTimeToKeep The MAXIMAL time period (in ms), a version can be kept.
	 */
	public CustomEpochPolicy(long minTimeToKeep, long period, long maxTimeToKeep) {
		super();
		this.minTimeToKeep = minTimeToKeep;
		this.period = period;
		this.maxTimeToKeep = maxTimeToKeep;
	}

	@Override
	public void onCommit(IEpoch epoch, IEpochHistory history) {
		final long now = System.currentTimeMillis();
		execute(now,  epoch, history);
	}

	/**
	 * Gets the set of periodically kept epochs for the given component.
	 * @param mvId The component's id
	 * @return the set of periodically kept epochs for the given component.
	 */
	protected SortedSet<IEpoch> getOrCreatePeriodicallyKept(String mvId) {
		SortedSet<IEpoch> l = this.periodicKeptById.get(mvId);
		if (l == null) {
			l = new TreeSet<>(EpochComparator.INSTANCE);
			SortedSet<IEpoch> previous = this.periodicKeptById.putIfAbsent(mvId, l);
			if (previous != null)
				l = previous;
		}
		return l;
	}

	/**
	 * Method to be called after each commit to release all unnecessary versions.
	 * @param currentTime The current time (in ms).
	 * @param currentEpoch The current (i.e. new committed) epoch.
	 * @param history The {@link IVersionHistory history} of the component
	 */
	protected void execute(final long currentTime, final IEpoch currentEpoch, final IEpochHistory history) {
		final SortedSet<IEpoch> periodicallyKept = getOrCreatePeriodicallyKept(history.getMultiVersionId());

		// Add the new committed epoch to the list of periodically kept if needed
		if (periodicallyKept.isEmpty() || currentEpoch.getTimestamp() - period >= periodicallyKept.last().getTimestamp() ) {
			periodicallyKept.add(currentEpoch);
		}

		history.releaseEpochs(new IPredicate<IEpoch>() {

			@Override
			public boolean test(IEpoch epoch) {
				final long time = epoch.getTimestamp();
				if (currentTime - time < minTimeToKeep) {
					// Rule 1: Keep the last minTimeToKeep ms
					return false;
				}
				if (currentTime - time >= maxTimeToKeep) {
					// Rule 3
					periodicallyKept.remove(epoch);
					return true;
				}

				// Rule 2: periodically kept
				return !periodicallyKept.contains(epoch);
			}
		});
	}
}
