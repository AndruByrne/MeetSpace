package com.pachakutech.meetspace;
import android.graphics.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import android.content.*;
import android.util.*;


public class VerticalTextView extends TextView {
	final boolean topDown;

	public VerticalTextView( Context context, AttributeSet attrs ) {
		super( context, attrs );
		final int gravity = getGravity( );
		if( Gravity.isVertical( gravity ) && ( gravity & Gravity.VERTICAL_GRAVITY_MASK ) == Gravity.BOTTOM ) {
			setGravity( ( gravity & Gravity.HORIZONTAL_GRAVITY_MASK ) | Gravity.TOP );
			topDown = false;
		} else
			topDown = true;
	}

	@Override
	protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec ) {
		super.onMeasure( heightMeasureSpec, widthMeasureSpec );
		setMeasuredDimension( getMeasuredHeight( ), getMeasuredWidth( ) );
	}

	@Override
	protected void onDraw( Canvas canvas ) {
		TextPaint textPaint = getPaint( ); 
		textPaint.setColor( getCurrentTextColor( ) );
		textPaint.drawableState = getDrawableState( );

		canvas.save( );

		if( topDown ) {
			canvas.translate( getWidth( ), 0 );
			canvas.rotate( 90 );
		} else {
			canvas.translate( 0, getHeight( ) );
			canvas.rotate( -90 );
		}


		canvas.translate( getCompoundPaddingLeft( ), getExtendedPaddingTop( ) );

		getLayout( ).draw( canvas );
		canvas.restore( );
	}
}
