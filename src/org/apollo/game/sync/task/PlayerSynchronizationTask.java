package org.apollo.game.sync.task;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apollo.game.event.impl.PlayerSynchronizationEvent;
import org.apollo.game.model.Player;
import org.apollo.game.model.Position;
import org.apollo.game.model.World;
import org.apollo.game.sync.block.AppearanceBlock;
import org.apollo.game.sync.block.ChatBlock;
import org.apollo.game.sync.block.SynchronizationBlock;
import org.apollo.game.sync.block.SynchronizationBlockSet;
import org.apollo.game.sync.seg.AddCharacterSegment;
import org.apollo.game.sync.seg.RemoveCharacterSegment;
import org.apollo.game.sync.seg.MovementSegment;
import org.apollo.game.sync.seg.SynchronizationSegment;
import org.apollo.game.sync.seg.TeleportSegment;
import org.apollo.util.CharacterRepository;

/**
 * A {@link SynchronizationTask} which synchronizes the specified
 * {@link Player}.
 * @author Graham
 */
public final class PlayerSynchronizationTask extends SynchronizationTask {

	/**
	 * The maximum number of players to load per cycle. This prevents the
	 * update packet from becoming too large (the client uses a 5000 byte
	 * buffer) and also stops old spec PCs from crashing when they login or
	 * teleport.
	 */
	private static final int NEW_PLAYERS_PER_CYCLE = 20;

	/**
	 * The player.
	 */
	private final Player player;

	/**
	 * Creates the {@link PlayerSynchronizationTask} for the specified player.
	 * @param player The player.
	 */
	public PlayerSynchronizationTask(Player player) {
		this.player = player;
	}

	@Override
	public void run() {
		Position lastKnownRegion = player.getLastKnownRegion();
		boolean regionChanged = player.hasRegionChanged();

		SynchronizationSegment segment;
		SynchronizationBlockSet blockSet = player.getBlockSet();
		if (blockSet.contains(ChatBlock.class)) {
			blockSet = blockSet.clone();
			blockSet.remove(ChatBlock.class);
		}

		if (player.isTeleporting()) {
			segment = new TeleportSegment(blockSet, player.getPosition());
		} else {
			segment = new MovementSegment(blockSet, player.getDirections());
		}

		List<Player> localPlayers = player.getLocalPlayerList();
		int oldLocalPlayers = localPlayers.size();
		List<SynchronizationSegment> segments = new ArrayList<SynchronizationSegment>();

		for (Iterator<Player> it = localPlayers.iterator(); it.hasNext(); ) {
			Player p = it.next();
			if (!p.isActive() || p.isTeleporting() || p.getPosition().getLongestDelta(player.getPosition()) > player.getViewingDistance()) {
				it.remove();
				segments.add(new RemoveCharacterSegment());
			} else {
				segments.add(new MovementSegment(p.getBlockSet(), p.getDirections()));
			}
		}

		int added = 0;

		CharacterRepository<Player> repository = World.getWorld().getPlayerRepository();
		for (Iterator<Player> it = repository.iterator(); it.hasNext(); ) {
			Player p = it.next();
			if (localPlayers.size() >= 255) {
				player.flagExcessivePlayers();
				break;
			} else if (added >= NEW_PLAYERS_PER_CYCLE) {
				break;
			}
			// we do not check p.isActive() here, since if they are active they must be in the repository
			if (p != player && p.getPosition().isWithinDistance(player.getPosition(), player.getViewingDistance()) && !localPlayers.contains(p)) {
				localPlayers.add(p);
				added++;

				blockSet = p.getBlockSet();
				if (!blockSet.contains(AppearanceBlock.class)) {
					// TODO check if client has cached appearance
					blockSet = blockSet.clone();
					blockSet.add(SynchronizationBlock.createPlayerAppearanceBlock(p));
				}

				segments.add(new AddCharacterSegment(blockSet, p.getIndex(), p.getPosition()));
			}
		}

		PlayerSynchronizationEvent event = new PlayerSynchronizationEvent(lastKnownRegion, player.getPosition(), regionChanged, segment, oldLocalPlayers, segments);
		player.send(event);
	}

}
