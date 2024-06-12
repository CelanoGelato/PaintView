package com.celano.base.ardrawingsketchandpaint.demo.paint.interfaces;

import android.graphics.Path;

import com.celano.base.ardrawingsketchandpaint.demo.paint.painttools.FirstCurrentPosition;

public interface Shapable {
	Path getPath();

	FirstCurrentPosition getFirstLastPoint();

	void setShap(ShapesInterface shape);
}
