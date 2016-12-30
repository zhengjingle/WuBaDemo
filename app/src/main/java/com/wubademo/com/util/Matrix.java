package com.wubademo.com.util;

/**
 * Created by zhengjingle on 2016/12/29.
 */

public class Matrix {
    
    public static void simple(int n, double[][] matrix) {
        for (int k = 0; k < n; k++) {
            if (matrix[k][k] == 0) {
                changeRow(n, k, matrix);
            }

            for (int i = 0; i < n; i++) {
                double temp = matrix[i][k];
                for (int j = 0; j < n + 1; j++) {
                    if (i < k)
                        break;
                    if (temp == 0)
                        continue;
                    if (temp != 1) {
                        matrix[i][j] /= temp;
                    }

                    if (i > k)
                        matrix[i][j] -= matrix[k][j];
                }
            }
        }

    }

    public static double[] getResult(int n, double[][] matrix) {
        double[] result = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            double temp = matrix[i][n];
            for (int j = n - 1; j >= 0; j--) {
                if (i < j && matrix[i][j] != 0) {
                    temp = temp - result[j] * matrix[i][j];
                }
            }
            temp /= matrix[i][i];
            result[i] = temp;

        }

        for (int k = 0; k < result.length; k++) {
            System.out.println("X" + (k + 1) + " = " + result[k]);
        }

        return result;
    }

    public static void changeRow(int n, int k, double[][] matrix) {
        double[] temp = new double[n + 1];
        // if()
        for (int i = k; i < n; i++) {
            if (i + 1 == n && matrix[k][k] == 0) {
                System.out.println("无解或有不唯一解！");
                return;
            }

            for (int j = 0; j < n + 1; j++) {
                temp[j] = matrix[k][j];
                matrix[k][j] = matrix[i + 1][j];
                matrix[i + 1][j] = temp[j];
            }
            if (matrix[k][k] != 0)
                return;

        }
    }
}
