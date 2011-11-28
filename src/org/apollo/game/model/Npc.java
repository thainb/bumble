package org.apollo.game.model;

import org.apollo.game.event.Event;

/**
 * A non-player character.
 * 
 * @author Blake Beaupain
 */
public class Npc extends Character {

	/**
	 * Instantiates a new {@code Npc}.
	 * 
	 * @param position
	 *            The initial position
	 */
	public Npc(Position position) {
		super(position);
	}

	@Override
	public void send(Event event) {

	}

}
