package classwork;

public class getter_setter {
    int a,b;
    int test_t;

    public getter_setter(int a, int b, int test_t) {
        this.a = a;
        this.b = b;
        this.test_t = test_t;
    }

    public int getTest_t() {
        return test_t;
    }

    public void setTest_t(int test_t) {
        this.test_t = test_t;
    }

    public getter_setter(int a, int b) {
        this.a = a;
        this.b = b;
    }

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }
}
