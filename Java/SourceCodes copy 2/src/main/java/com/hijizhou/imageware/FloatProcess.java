package com.hijizhou.imageware;

import ij.ImageStack;

import java.awt.*;

/**
 * Class FloatProcess.
 *
 * @author	Daniel Sage
 *			Biomedical Imaging Group
 *			Ecole Polytechnique Federale de Lausanne, Lausanne, Switzerland
 */

public class FloatProcess extends FloatPointwise implements Process {

	//------------------------------------------------------------------
	//
	//	Constructors section
	//
	//------------------------------------------------------------------
	protected FloatProcess(int nx, int ny, int nz) 		{ super(nx, ny, nz); }
	protected FloatProcess(Image image, int mode) 		{ super(image, mode); }

	protected FloatProcess(ImageStack stack, int mode) 	{ super(stack, mode); }
	protected FloatProcess(ImageStack stack, byte chan) { super(stack, chan); }

	protected FloatProcess(byte[] array, int mode) 		{ super(array, mode); }
	protected FloatProcess(byte[][] array, int mode) 	{ super(array, mode); }
	protected FloatProcess(byte[][][] array, int mode) 	{ super(array, mode); }
	protected FloatProcess(short[] array, int mode) 	{ super(array, mode); }
	protected FloatProcess(short[][] array, int mode) 	{ super(array, mode); }
	protected FloatProcess(short[][][] array, int mode) { super(array, mode); }
	protected FloatProcess(float[] array, int mode) 	{ super(array, mode); }
	protected FloatProcess(float[][] array, int mode) 	{ super(array, mode); }
	protected FloatProcess(float[][][] array, int mode) { super(array, mode); }
	protected FloatProcess(double[] array, int mode) 	{ super(array, mode); }
	protected FloatProcess(double[][] array, int mode) 	{ super(array, mode); }
	protected FloatProcess(double[][][] array, int mode){ super(array, mode); }
	
	/**
	* Apply a separable gaussian smoothing over the image with the same
	* strengthness in all directions. 
	* To have a smmothing effect the strengthness should be strictly 
	* greater than 0 and the size in the considered directions 
	* should be greater strictly than 1.
	*
	* @param sigma		Strengthness of the smoothing
	*/
	@Override
	public void smoothGaussian(double sigma) {
		smoothGaussian(sigma, sigma, sigma);
	}
	
	/**
	* Apply a separablegaussian smoothing over the image with an 
	* independant strengthness in the different directions.
	* To have a smmothing effect the strengthness should be strictly 
	* greater than 0 and the size in the considered directions 
	* should be greater strictly than 1.
	*
	* @param sigmaX		Strengthness of the smoothing in X axis
	* @param sigmaY		Strengthness of the smoothing in X axis
	* @param sigmaZ		Strengthness of the smoothing in X axis
	*/
	@Override
	public void smoothGaussian(double sigmaX, double sigmaY, double sigmaZ) {
		int n = 3;
		double N = n;
		double poles[] = new double[n];

		if (nx > 1 && sigmaX > 0.0) {
			double s2 = sigmaX * sigmaX;
			double alpha = 1.0 + (N/s2) - (Math.sqrt(N*N+2*N*s2)/s2);
			poles[0] = poles[1] = poles[2] = alpha;
			double line[]  = new double[nx];
			for (int z=0; z<nz; z++) {
				for (int y=0; y<ny; y++) {
					getX(0, y, z, line);
					putX(0, y, z, Convolver.convolveIIR(line, poles));
				}
			}
		}

		if (ny > 1 && sigmaY > 0.0) {
			double s2 = sigmaY * sigmaY;
			double alpha = 1.0 + (N/s2) - (Math.sqrt(N*N+2*N*s2)/s2);
			poles[0] = poles[1] = poles[2] = alpha;
			double line[]  = new double[ny];
			for (int x=0; x<nx; x++) {
				for (int z=0; z<nz; z++) {
					getY(x, 0, z, line);
					putY(x, 0, z, Convolver.convolveIIR(line, poles));
				}
			}
		}

		if (nz > 1 && sigmaZ > 0.0) {
			double s2 = sigmaZ * sigmaZ;
			double alpha = 1.0 + (N/s2) - (Math.sqrt(N*N+2*N*s2)/s2);
			poles[0] = poles[1] = poles[2] = alpha;
			double line[]  = new double[nz];
			for (int y=0; y<ny; y++) {
				for (int x=0; x<nx; x++) {
					getZ(x, y, 0, line);
					putZ(x, y, 0, Convolver.convolveIIR(line, poles));
				}
			}
		}
	}

	/**
	* Get the maximum of this imageware and a imageware.
	*
	* @param imageware	imageware to max
	*/
	@Override
	public void max(ImageWare imageware) {
		if (!isSameSize(imageware)) {
			throw new ArrayStoreException(
				"\n-------------------------------------------------------\n" +
				"Error in imageware package\n" +
				"Unable to get the maximum because the two operands are not the same size.\n" + 
				"[" + nx + "," + ny + "," + "," + nz + "] != " + 
				"[" + imageware.getSizeX() + "," + imageware.getSizeY() + "," + imageware.getSizeZ() + "].\n" +
				"-------------------------------------------------------\n"
			);
		}
		switch(imageware.getType()) {
		case ImageWare.BYTE:
			for(int z=0; z<nz; z++) {
				byte[] tmp = ((ByteSet)imageware).getSliceByte(z);
				for(int k=0; k<nxy; k++) {
					if (((float[])data[z])[k] < tmp[k])
						((float[])data[z])[k] = tmp[k];
				}
			}	
			break;
		case ImageWare.SHORT:
			for(int z=0; z<nz; z++) {
				short[] tmp = ((ShortSet)imageware).getSliceShort(z);
				for(int k=0; k<nxy; k++) {
					if (((float[])data[z])[k] < tmp[k])
						((float[])data[z])[k] = tmp[k];
				}
			}	
			break;
		case ImageWare.FLOAT:
			for(int z=0; z<nz; z++) {
				float[] tmp = ((FloatSet)imageware).getSliceFloat(z);
				for(int k=0; k<nxy; k++) {
					if (((float[])data[z])[k] < tmp[k])
						((float[])data[z])[k] = tmp[k];
				}
			}	
			break;
		case ImageWare.DOUBLE:
			for(int z=0; z<nz; z++) {
				double[] tmp = ((DoubleSet)imageware).getSliceDouble(z);
				for(int k=0; k<nxy; k++) {
					if (((float[])data[z])[k] < (float)tmp[k])
						((float[])data[z])[k] = (float)tmp[k];
				}
			}	
			break;
		default :
			throw new ArrayStoreException(
				"\n-------------------------------------------------------\n" +
				"Error in imageware package\n" +
				"Unknown type " + imageware.getType() + "].\n" +
				"-------------------------------------------------------\n"
			);
		}
	}

	/**
	* Get the minimum of this imageware and a imageware.
	*
	* @param imageware	imageware to min
	*/
	@Override
	public void min(ImageWare imageware) {
		if (!isSameSize(imageware)) {
			throw new ArrayStoreException(
				"\n-------------------------------------------------------\n" +
				"Error in imageware package\n" +
				"Unable to get the minimum because the two operands are not the same size.\n" + 
				"[" + nx + "," + ny + "," + "," + nz + "] != " + 
				"[" + imageware.getSizeX() + "," + imageware.getSizeY() + "," + imageware.getSizeZ() + "].\n" +
				"-------------------------------------------------------\n"
			);
		}
		switch(imageware.getType()) {
		case ImageWare.BYTE:
			for(int z=0; z<nz; z++) {
				byte[] tmp = ((ByteSet)imageware).getSliceByte(z);
				for(int k=0; k<nxy; k++) {
					if (((float[])data[z])[k] > tmp[k])
						((float[])data[z])[k] = tmp[k];
				}
			}	
			break;
		case ImageWare.SHORT:
			for(int z=0; z<nz; z++) {
				short[] tmp = ((ShortSet)imageware).getSliceShort(z);
				for(int k=0; k<nxy; k++) {
					if (((float[])data[z])[k] > tmp[k])
						((float[])data[z])[k] = tmp[k];
				}
			}	
			break;
		case ImageWare.FLOAT:
			for(int z=0; z<nz; z++) {
				float[] tmp = ((FloatSet)imageware).getSliceFloat(z);
				for(int k=0; k<nxy; k++) {
					if (((float[])data[z])[k] > tmp[k])
						((float[])data[z])[k] = tmp[k];
				}
			}	
			break;
		case ImageWare.DOUBLE:
			for(int z=0; z<nz; z++) {
				double[] tmp = ((DoubleSet)imageware).getSliceDouble(z);
				for(int k=0; k<nxy; k++) {
					if (((float[])data[z])[k] > (float)tmp[k])
						((float[])data[z])[k] = (float)tmp[k];
				}
			}	
			break;
		default :
			throw new ArrayStoreException(
				"\n-------------------------------------------------------\n" +
				"Error in imageware package\n" +
				"Unknown type " + imageware.getType() + "].\n" +
				"-------------------------------------------------------\n"
			);
		}
	}

	/**
	* Add a imageware to the current imageware.
	*
	* @param imageware	imageware to add
	*/
	@Override
	public void add(ImageWare imageware) {
		if (!isSameSize(imageware)) {
			throw new ArrayStoreException(
				"\n-------------------------------------------------------\n" +
				"Error in imageware package\n" +
				"Unable to add because the two operands are not the same size.\n" + 
				"[" + nx + "," + ny + "," + "," + nz + "] != " + 
				"[" + imageware.getSizeX() + "," + imageware.getSizeY() + "," + imageware.getSizeZ() + "].\n" +
				"-------------------------------------------------------\n"
			);
		}
		switch(imageware.getType()) {
		case ImageWare.BYTE:
			for(int z=0; z<nz; z++) {
				byte[] tmp = ((ByteSet)imageware).getSliceByte(z);
				for(int k=0; k<nxy; k++) {
					((float[])data[z])[k] += tmp[k];
				}
			}	
			break;
		case ImageWare.SHORT:
			for(int z=0; z<nz; z++) {
				short[] tmp = ((ShortSet)imageware).getSliceShort(z);
				for(int k=0; k<nxy; k++) {
					((float[])data[z])[k] += tmp[k];
				}
			}	
			break;
		case ImageWare.FLOAT:
			for(int z=0; z<nz; z++) {
				float[] tmp = ((FloatSet)imageware).getSliceFloat(z);
				for(int k=0; k<nxy; k++) {
					((float[])data[z])[k] += tmp[k];
				}
			}	
			break;
		case ImageWare.DOUBLE:
			for(int z=0; z<nz; z++) {
				double[] tmp = ((DoubleSet)imageware).getSliceDouble(z);
				for(int k=0; k<nxy; k++) {
					((float[])data[z])[k] += (float)tmp[k];
				}
			}	
			break;
		default :
			throw new ArrayStoreException(
				"\n-------------------------------------------------------\n" +
				"Error in imageware package\n" +
				"Unknown type " + imageware.getType() + "].\n" +
				"-------------------------------------------------------\n"
			);
		}
	}

	/**
	* Multiply a imageware to the current imageware.
	*
	* @param imageware	imageware to multiply
	*/
	@Override
	public void multiply(ImageWare imageware) {
		if (!isSameSize(imageware)) {
			throw new ArrayStoreException(
				"\n-------------------------------------------------------\n" +
				"Error in imageware package\n" +
				"Unable to multiply because the two operands are not the same size.\n" + 
				"[" + nx + "," + ny + "," + "," + nz + "] != " + 
				"[" + imageware.getSizeX() + "," + imageware.getSizeY() + "," + imageware.getSizeZ() + "].\n" +
				"-------------------------------------------------------\n"
			);
		}
		switch(imageware.getType()) {
		case ImageWare.BYTE:
			for(int z=0; z<nz; z++) {
				byte[] tmp = ((ByteSet)imageware).getSliceByte(z);
				for(int k=0; k<nxy; k++) {
					((float[])data[z])[k] *= tmp[k];
				}
			}	
			break;
		case ImageWare.SHORT:
			for(int z=0; z<nz; z++) {
				short[] tmp = ((ShortSet)imageware).getSliceShort(z);
				for(int k=0; k<nxy; k++) {
					((float[])data[z])[k] *= tmp[k];
				}
			}	
			break;
		case ImageWare.FLOAT:
			for(int z=0; z<nz; z++) {
				float[] tmp = ((FloatSet)imageware).getSliceFloat(z);
				for(int k=0; k<nxy; k++) {
					((float[])data[z])[k] *= tmp[k];
				}
			}	
			break;
		case ImageWare.DOUBLE:
			for(int z=0; z<nz; z++) {
				double[] tmp = ((DoubleSet)imageware).getSliceDouble(z);
				for(int k=0; k<nxy; k++) {
					((float[])data[z])[k] *= (float)tmp[k];
				}
			}	
			break;
		default :
			throw new ArrayStoreException(
				"\n-------------------------------------------------------\n" +
				"Error in imageware package\n" +
				"Unknown type " + imageware.getType() + "].\n" +
				"-------------------------------------------------------\n"
			);
		}
	}

	/**
	* Subtract a imageware to the current imageware.
	*
	* @param imageware	imageware to subtract
	*/
	@Override
	public void subtract(ImageWare imageware) {
		if (!isSameSize(imageware)) {
			throw new ArrayStoreException(
				"\n-------------------------------------------------------\n" +
				"Error in imageware package\n" +
				"Unable to subtract because the two operands are not the same size.\n" + 
				"[" + nx + "," + ny + "," + "," + nz + "] != " + 
				"[" + imageware.getSizeX() + "," + imageware.getSizeY() + "," + imageware.getSizeZ() + "].\n" +
				"-------------------------------------------------------\n"
			);
		}
		switch(imageware.getType()) {
		case ImageWare.BYTE:
			for(int z=0; z<nz; z++) {
				byte[] tmp = ((ByteSet)imageware).getSliceByte(z);
				for(int k=0; k<nxy; k++) {
					((float[])data[z])[k] -= tmp[k];
				}
			}	
			break;
		case ImageWare.SHORT:
			for(int z=0; z<nz; z++) {
				short[] tmp = ((ShortSet)imageware).getSliceShort(z);
				for(int k=0; k<nxy; k++) {
					((float[])data[z])[k] -= tmp[k];
				}
			}	
			break;
		case ImageWare.FLOAT:
			for(int z=0; z<nz; z++) {
				float[] tmp = ((FloatSet)imageware).getSliceFloat(z);
				for(int k=0; k<nxy; k++) {
					((float[])data[z])[k] -= tmp[k];
				}
			}	
			break;
		case ImageWare.DOUBLE:
			for(int z=0; z<nz; z++) {
				double[] tmp = ((DoubleSet)imageware).getSliceDouble(z);
				for(int k=0; k<nxy; k++) {
					((float[])data[z])[k] -= (float)tmp[k];
				}
			}	
			break;
		default :
			throw new ArrayStoreException(
				"\n-------------------------------------------------------\n" +
				"Error in imageware package\n" +
				"Unknown type " + imageware.getType() + "].\n" +
				"-------------------------------------------------------\n"
			);
		}
	}

	/**
	* Divide a imageware to the current imageware.
	*
	* @param imageware	imageware to divide
	*/
	@Override
	public void divide(ImageWare imageware) {
		if (!isSameSize(imageware)) {
			throw new ArrayStoreException(
				"\n-------------------------------------------------------\n" +
				"Error in imageware package\n" +
				"Unable to divide because the two operands are not the same size.\n" + 
				"[" + nx + "," + ny + "," + "," + nz + "] != " + 
				"[" + imageware.getSizeX() + "," + imageware.getSizeY() + "," + imageware.getSizeZ() + "].\n" +
				"-------------------------------------------------------\n"
			);
		}
		switch(imageware.getType()) {
		case ImageWare.BYTE:
			for(int z=0; z<nz; z++) {
				byte[] tmp = ((ByteSet)imageware).getSliceByte(z);
				for(int k=0; k<nxy; k++) {
					((float[])data[z])[k] /= tmp[k];
				}
			}	
			break;
		case ImageWare.SHORT:
			for(int z=0; z<nz; z++) {
				short[] tmp = ((ShortSet)imageware).getSliceShort(z);
				for(int k=0; k<nxy; k++) {
					((float[])data[z])[k] /= tmp[k];
				}
			}	
			break;
		case ImageWare.FLOAT:
			for(int z=0; z<nz; z++) {
				float[] tmp = ((FloatSet)imageware).getSliceFloat(z);
				for(int k=0; k<nxy; k++) {
					((float[])data[z])[k] /= tmp[k];
				}
			}	
			break;
		case ImageWare.DOUBLE:
			for(int z=0; z<nz; z++) {
				double[] tmp = ((DoubleSet)imageware).getSliceDouble(z);
				for(int k=0; k<nxy; k++) {
					((float[])data[z])[k] /= (float)tmp[k];
				}
			}	
			break;
		default :
			throw new ArrayStoreException(
				"\n-------------------------------------------------------\n" +
				"Error in imageware package\n" +
				"Unknown type " + imageware.getType() + "].\n" +
				"-------------------------------------------------------\n"
			);
		}
	}
	

}	// end of class
