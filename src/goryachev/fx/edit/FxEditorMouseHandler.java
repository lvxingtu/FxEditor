// Copyright © 2016-2017 Andy Goryachev <andy@goryachev.com>
package goryachev.fx.edit;
import goryachev.common.util.D;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;


/**
 * FxEditor Mouse Handler.
 */
public class FxEditorMouseHandler
{
	protected final FxEditor editor;
	protected final SelectionController selector;
	protected boolean dragging;
	protected boolean draggingScroll;


	public FxEditorMouseHandler(FxEditor ed, SelectionController sel)
	{
		this.editor = ed;
		this.selector = sel;
	}
	
	
	protected boolean isOverScrollBars(double x, double y)
	{
		if(x >= editor.vscroll.getLayoutX())
		{
			return true;
		}
		else if(y >= editor.hscroll.getLayoutY())
		{
			return true;
		}
		return false;
	}
	
	
	protected void handleScroll(ScrollEvent ev)
	{
		if(isOverScrollBars(ev.getX(), ev.getY()))
		{
			return;
		}
		
		if(ev.isShiftDown())
		{
			// TODO horizontal scroll perhaps?
			D.print("horizontal scroll", ev.getDeltaX());
		}
		else if(ev.isShortcutDown())
		{
			// page up / page down
			if(ev.getDeltaY() >= 0)
			{
				editor.pageUp();
			}
			else
			{
				editor.pageDown();
			}
		}
		else
		{
			// vertical block scroll
			editor.blockScroll(ev.getDeltaY() >= 0);
		}
	}
	
	
	protected Marker getTextPos(MouseEvent ev)
	{
		double x = ev.getScreenX();
		double y = ev.getScreenY();
		return editor.getTextPos(x, y);
	}
	
	
	protected void handleMousePressed(MouseEvent ev)
	{
		if(isOverScrollBars(ev.getX(), ev.getY()))
		{
			return;
		}
			
		Marker pos = getTextPos(ev);
		editor.setSuppressBlink(true);
		
		if(ev.isShiftDown())
		{
			// FIX there might be a zero length segment (single caret)
			
			// expand selection from the anchor point to the current position
			// clearing existing (possibly multiple) selection
			selector.clearAndExtendLastSegment(pos);
		}
		else if(ev.isShortcutDown())
		{
			if(selector.isInsideSelection(pos) || (!editor.isMultipleSelectionEnabled()))
			{
				selector.setSelection(pos);
			}
			else
			{
				// add a new caret
				selector.addSelectionSegment(pos, pos);
			}
		}
		else
		{
			editor.clearSelection();
			if(pos != null)
			{
				selector.addSelectionSegment(pos, pos);
			}
		}
		
		editor.requestFocus();
	}
	
	
	protected void handleMouseDragged(MouseEvent ev)
	{
		if(draggingScroll)
		{
			return;
		}
		
		if(isOverScrollBars(ev.getX(), ev.getY()))
		{
			dragging = false;
			draggingScroll = true;
			return;
		}
		
		dragging = true;
		
		Marker pos = getTextPos(ev);
		selector.extendLastSegment(pos);
	}
	
	
	protected void handleMouseReleased(MouseEvent ev)
	{
		dragging = false;
		draggingScroll = false;
		editor.setSuppressBlink(false);

		if(isOverScrollBars(ev.getX(), ev.getY()))
		{
			return;
		}
		
		selector.commitSelection();
	}
}