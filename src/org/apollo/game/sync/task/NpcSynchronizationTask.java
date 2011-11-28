package org.apollo.game.sync.task;

import org.apollo.game.model.Player;

/**
 * A {@link SynchronizationTask} that synchronizes all world {@link Npc}s for a
 * {@link Player}.
 * 
 * @author Blake Beaupain
 */
public class NpcSynchronizationTask extends SynchronizationTask {

	/** The maximum amount of new NPCs to add per cycle. */
	public static final int NEW_NPCS_PER_CYCLE = 20;

	/** The player to synchronize for. */
	private final Player player;

	/**
	 * Instantiates a new {@code NpcSynchronizationTask}.
	 * 
	 * @param player
	 *            The player to synchronize for
	 */
	public NpcSynchronizationTask(Player player) {
		this.player = player;
	}

	@Override
	public void run() {

	}

}
