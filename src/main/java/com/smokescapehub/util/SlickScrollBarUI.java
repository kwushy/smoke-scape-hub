package com.smokescapehub.util;

import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.plaf.basic.BasicScrollBarUI;
import net.runelite.client.ui.ColorScheme;

// A thin, dark, arrow-less scrollbar matching RuneLite's own look (same
// colors as its ScrollBar.track / ScrollBar.thumb LAF properties). Colors
// are hardcoded here rather than relying on RuneLiteScrollBarUI picking up
// those global theme properties, which wasn't rendering correctly.
public class SlickScrollBarUI extends BasicScrollBarUI
{
	private static final int WIDTH = 7;

	@Override
	protected void configureScrollBarColors()
	{
		thumbColor = ColorScheme.MEDIUM_GRAY_COLOR;
		thumbDarkShadowColor = ColorScheme.MEDIUM_GRAY_COLOR;
		thumbLightShadowColor = ColorScheme.MEDIUM_GRAY_COLOR;
		thumbHighlightColor = ColorScheme.MEDIUM_GRAY_COLOR;
		trackColor = ColorScheme.SCROLL_TRACK_COLOR;
		trackHighlightColor = ColorScheme.SCROLL_TRACK_COLOR;
	}

	@Override
	protected JButton createDecreaseButton(int orientation)
	{
		return zeroSizeButton();
	}

	@Override
	protected JButton createIncreaseButton(int orientation)
	{
		return zeroSizeButton();
	}

	@Override
	public Dimension getPreferredSize(JComponent c)
	{
		Dimension size = super.getPreferredSize(c);
		return scrollbar.getOrientation() == JScrollBar.VERTICAL
			? new Dimension(WIDTH, size.height)
			: new Dimension(size.width, WIDTH);
	}

	private JButton zeroSizeButton()
	{
		JButton button = new JButton();
		Dimension zero = new Dimension(0, 0);
		button.setPreferredSize(zero);
		button.setMinimumSize(zero);
		button.setMaximumSize(zero);
		return button;
	}
}
