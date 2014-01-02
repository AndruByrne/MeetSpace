package com.pachakutech.meetspace;

import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import com.pachakutech.meetspace.*;
import android.widget.GridLayout.*;
import android.graphics.Paint.*;
import android.widget.*;

public class VerticalAutoFitTextView extends TextView {
    private TextPaint textPaint;
    private String text;
    private int ascent;
    private Rect text_bounds = new Rect( );
	private float minTextSize;
	private float maxTextSize;
	private float text_horizontally_centered_origin_x;
	private float text_horizontally_centered_origin_y;
	
    final static int DEFAULT_TEXT_SIZE = 15;

    public VerticalAutoFitTextView( Context context ) {
        super( context );
        initView( );
    }

    public VerticalAutoFitTextView( Context context, AttributeSet attrs ) {
        super( context, attrs );
        initView( );

        TypedArray a = context.obtainStyledAttributes( attrs, R.styleable.VerticalLabelView );

//        CharSequence s = a.getString(R.styleable.VerticalLabelView_text);
//        if (s != null) setText(s.toString());

        setTextColor( a.getColor( R.styleable.VerticalLabelView_textColor, 0xFF000000 ) );

		//      int textSize = a.getDimensionPixelOffset(R.styleable.VerticalLabelView_textSize, 0);
		//      if (textSize > 0) setTextSize(textSize);

        a.recycle( );
    }

    private final void initView( ) {

        textPaint = new TextPaint( );
        textPaint.setAntiAlias( true );
        textPaint.setColor( 0xFF000000 );
		textPaint.setTextAlign( Align.CENTER );
        setPadding( 3, 3, 3, 3 );

//		maxTextSize = textPaint.getTextSize();
		maxTextSize = 128;
		if( maxTextSize < 35 ) {
			maxTextSize = 30;
		}
		minTextSize = 20;
    }

    public void setText( String setText ) {
        text = setText;
        requestLayout( );
        invalidate( );
    }

    public void setTextSize( int size ) {
        textPaint.setTextSize( size );
        requestLayout( );
        invalidate( );
    }

    public void setTextColor( int color ) {
        textPaint.setColor( color );
        invalidate( );
    }

	private void refitText( String text, int textHeight ) {
		if( textHeight > 0 ) {

			int availableHeight = textHeight - this.getPaddingLeft( )
                - this.getPaddingRight( );
			float trySize = maxTextSize;

			textPaint.setTextSize( trySize );
			Log.e( MeetSpace.TAG, "trysize: " + trySize );
			while( ( trySize > minTextSize )
				  && ( textPaint.measureText( text ) > availableHeight ) ) {
				Log.e( MeetSpace.TAG, "Refitting with available height = " + availableHeight + "and trysize: " + trySize );
				trySize -= 1;
				if( trySize <= minTextSize ) {
					trySize = minTextSize;
					break;
				}
				textPaint.setTextSize( trySize );
			}
			textPaint.setTextSize( trySize );
		}
	}

	@Override
	protected void onTextChanged( final CharSequence text, final int start,
								 final int before, final int after ) {
		refitText( text.toString( ), this.getHeight( ) );
	}

	@Override
	protected void onSizeChanged( int w, int h, int oldw, int oldh ) {
		if( w != oldw ) {
			refitText( text.toString( ), h );
		}
	}

    @Override
    protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec ) {

        textPaint.getTextBounds( text, 0, text.length( ), text_bounds );
        setMeasuredDimension(
			measureWidth( widthMeasureSpec ),
			measureHeight( heightMeasureSpec ) );
//		int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
		refitText( text.toString( ), 
				  MeasureSpec.getSize( heightMeasureSpec ) );
    }

    private int measureWidth( int measureSpec ) {
        int result = 0;
        int specMode = MeasureSpec.getMode( measureSpec );
        int specSize = MeasureSpec.getSize( measureSpec );

        if( specMode == MeasureSpec.EXACTLY ) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text
            //result = text_bounds.height( ) + getPaddingLeft( ) + getPaddingRight( );
			result = text_bounds.height( ) + getPaddingTop() + getPaddingBottom();
			
            if( specMode == MeasureSpec.AT_MOST ) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min( result, specSize );
            }
        }
        return result;
    }

    private int measureHeight( int measureSpec ) {
        int result = 0;
        int specMode = MeasureSpec.getMode( measureSpec );
        int specSize = MeasureSpec.getSize( measureSpec );

        ascent = (int) textPaint.ascent( );
        if( specMode == MeasureSpec.EXACTLY ) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text
            result = text_bounds.width( ) + getPaddingTop( ) + getPaddingBottom( );

            if( specMode == MeasureSpec.AT_MOST ) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min( result, specSize );
            }
        }
        return result;
    }

	public float getMinTextSize( ) {
		return minTextSize;
	}

	public void setMinTextSize( int minTextSize ) {
		this.minTextSize = minTextSize;
	}

	public float getMaxTextSize( ) {
		return maxTextSize;
	}

	public void setMaxTextSize( int minTextSize ) {
		this.maxTextSize = minTextSize;
	}


    @Override
    protected void onDraw( Canvas canvas ) {
        super.onDraw( canvas );

        text_horizontally_centered_origin_x = getPaddingLeft( ) + text_bounds.width( ) / 2f;
        text_horizontally_centered_origin_y = getPaddingTop( ) - ascent;

        canvas.translate( text_horizontally_centered_origin_y, text_horizontally_centered_origin_x );
        canvas.rotate( -90 );
        canvas.drawText( text, 0, 0, textPaint );
    }
}

