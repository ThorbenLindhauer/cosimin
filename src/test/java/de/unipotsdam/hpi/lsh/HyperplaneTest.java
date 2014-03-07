package de.unipotsdam.hpi.lsh;

import org.junit.Assert;
import org.junit.Test;

public class HyperplaneTest {

	@Test
	public void testScalarProduct() {
		int[] vector1 = new int[] { 0, 1, 2, 3 };
		int[] vector2 = new int[] { 2, 3, 1, -4 };
		double expectedScalarProduct = 0 + 3 + 2 - 12;
		Hyperplane hyperplane = new DenseHyperplane(vector1);
		double actualScalarProduct = hyperplane.scalarProduct(vector2);
		Assert.assertEquals(expectedScalarProduct, actualScalarProduct, 0.0001d);

		vector1 = new int[] { 2, 3, 1, -4 };
		vector2 = new int[] { 0, 1, 2, 3 };
		hyperplane = new DenseHyperplane(vector1);
		actualScalarProduct = hyperplane.scalarProduct(vector2);
		Assert.assertEquals(expectedScalarProduct, actualScalarProduct, 0.0001d);
	}

	@Test
	public void testIsOnPositiveSide() {
		int[] normalVector = new int[] { 0, 0, 0, 1 };
		Hyperplane hyperplane = new SparseHyperplane(normalVector);

		int[] vector1 = new int[] { 0, 1, 2, 3 };
		int[] vector2 = new int[] { 2, 3, 1, -4 };

		Assert.assertTrue(hyperplane.isOnPositiveSide(vector1));
		Assert.assertFalse(hyperplane.isOnPositiveSide(vector2));
	}
}
