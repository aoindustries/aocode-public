package com.aoindustries.awt;

/*
 * Copyright 2001-2008 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
import java.awt.*;

/**
 * Matches the height of another panel while maintaining its own width
 *
 * @version  1.0
 *
 * @author  AO Industries, Inc.
 */
public class MatchHeightPanel extends Panel {

	private final Panel other;
public MatchHeightPanel(Panel other, LayoutManager layout) {
	super(layout);
	this.other=other;
}
public Dimension getPreferredSize() {
	return new Dimension(
		super.getPreferredSize().width,
		other.getPreferredSize().height
	);
}
}
