/*
 * Copyright (c) 2001-2025 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://robocode.sourceforge.io/license/epl-v10.html
 */
package robocode;


import net.sf.robocode.peer.IRobotStatics;
import net.sf.robocode.security.SafeComponent;
import net.sf.robocode.serialization.ISerializableHelper;
import net.sf.robocode.serialization.RbSerializer;
import robocode.robotinterfaces.IBasicRobot;
import robocode.robotinterfaces.IInteractiveEvents;
import robocode.robotinterfaces.IInteractiveRobot;

import java.awt.*;
import java.nio.ByteBuffer;


/**
 * A KeyTypedEvent is sent to {@link Robot#onKeyTyped(java.awt.event.KeyEvent)
 * onKeyTyped()} when a key has been typed (pressed and released) on the keyboard.
 *
 * @see KeyPressedEvent
 * @see KeyReleasedEvent
 *
 * @author Pavel Savara (original)
 * @author Flemming N. Larsen (contributor)
 *
 * @since 1.6.1
 */
public final class KeyTypedEvent extends KeyEvent {
	private static final long serialVersionUID = 1L;
	private final static int DEFAULT_PRIORITY = 98;

	/**
	 * Called by the game to create a new KeyTypedEvent.
	 *
	 * @param source the source key event originating from the AWT.
	 */
	public KeyTypedEvent(java.awt.event.KeyEvent source) {
		super(source);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
    int getDefaultPriority() {
		return DEFAULT_PRIORITY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
    void dispatch(IBasicRobot robot, IRobotStatics statics, Graphics2D graphics) {
		if (statics.isInteractiveRobot()) {
			IInteractiveEvents listener = ((IInteractiveRobot) robot).getInteractiveEventListener();

			if (listener != null) {
				listener.onKeyTyped(getSourceEvent());
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	byte getSerializationType() {
		return RbSerializer.KeyTypedEvent_TYPE;
	}

	static ISerializableHelper createHiddenSerializer() {
		return new SerializableHelper();
	}

	private static class SerializableHelper implements ISerializableHelper {

		public int sizeOf(RbSerializer serializer, Object object) {
			return RbSerializer.SIZEOF_TYPEINFO + RbSerializer.SIZEOF_CHAR + RbSerializer.SIZEOF_INT
					+ RbSerializer.SIZEOF_INT + RbSerializer.SIZEOF_LONG + RbSerializer.SIZEOF_INT + RbSerializer.SIZEOF_INT;
		}

		public void serialize(RbSerializer serializer, ByteBuffer buffer, Object object) {
			KeyTypedEvent obj = (KeyTypedEvent) object;
			java.awt.event.KeyEvent src = obj.getSourceEvent();

			serializer.serialize(buffer, src.getKeyChar());
			serializer.serialize(buffer, src.getKeyCode());
			serializer.serialize(buffer, src.getKeyLocation());
			serializer.serialize(buffer, src.getID());
			serializer.serialize(buffer, src.getModifiersEx());
			serializer.serialize(buffer, src.getWhen());
		}

		public Object deserialize(RbSerializer serializer, ByteBuffer buffer) {
			char keyChar = buffer.getChar();
			int keyCode = buffer.getInt();
			int keyLocation = buffer.getInt();
			int id = buffer.getInt();
			int modifiersEx = buffer.getInt();
			long when = buffer.getLong();

			return new KeyTypedEvent(
					new java.awt.event.KeyEvent(SafeComponent.getSafeEventComponent(), id, when, modifiersEx, keyCode, keyChar,
					keyLocation));
		}
	}
}
