import java.security.SecureRandom

/**
 * Created by stas on 22.11.15.
 */



class RSA {




    static int CRT = 1

    BigInteger d, e

    synchronized BigInteger n
    synchronized BigInteger fn = BigInteger.ONE
    synchronized BigInteger keyPartsMult = BigInteger.ONE
    synchronized def M = BigInteger.ZERO

    synchronized def keyParts = new ArrayList<BigInteger>()


    class MyThread extends Thread {
        int i
        int l
        MyThread(int l, int i) {
            this.i = i
            this.l = l

        }

        public void run() {
            def prime = generatePrimeCRT(l, i)
            multiplyN(prime)

        }
    }

    static class PrimeThread extends Thread {

        int bits
        PrimeThread(int bits) {
            this.bits = bits

        }

        public void run() {

            def r = new SecureRandom()
            def prime = new BigInteger(bits, 100, r)

            /*for (int i = 0; i < 50; i++)
                if (!RabinMillerTest(prime)) {
                    println "Generated number is not prime"
                    break
                }*/


        }
    }


    private static boolean RabinMillerTest(BigInteger n) {

        // Ensures that temp > 1 and temp < n.
        BigInteger temp = BigInteger.ZERO;
        Random r = new Random();


        while ({

            temp = new BigInteger(n.bitLength()-1, r);

            temp.compareTo(BigInteger.ONE) <= 0
        }());



        // Screen out n if our random number happens to share a factor with n.
        if (!n.gcd(temp).equals(BigInteger.ONE)) return false;

        // For debugging, prints out the integer to test with.
        //System.out.println("Testing with " + temp);

        BigInteger base = n.subtract(BigInteger.ONE);
        BigInteger TWO = new BigInteger("2");

        // Figure out the largest power of two that divides evenly into n-1.
        int k=0;
        while ( (base.mod(TWO)).equals(BigInteger.ZERO)) {
            base = base.divide(TWO);
            k++;
        }

        // This is the odd value r, as described in our text.
        //System.out.println("base is " + base);

        BigInteger curValue = temp.modPow(base,n);

        // If this works out, we just say it's prime.
        if (curValue.equals(BigInteger.ONE))
            return true;

        // Otherwise, we will check to see if this value successively
        // squared ever yields -1.
        for (int i=0; i<k; i++) {

            // We need to really check n-1 which is equivalent to -1.
            if (curValue.equals(n.subtract(BigInteger.ONE)))
                return true;

            // Square this previous number - here I am just doubling the
            // exponent. A more efficient implementation would store the
            // value of the exponentiation and square it mod n.
            else
                curValue = curValue.modPow(TWO, n);
        }

        // If none of our tests pass, we return false. The number is
        // definitively composite if we ever get here.
        return false;
    }



    public RSA(int bits)
    {

        def r = new SecureRandom()

        def p = new BigInteger(bits, 100, r)
        def q = new BigInteger(bits, 100, r)

        n = p.multiply(q)

        fn = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE))
        e = new BigInteger(65537)


        d = e.modInverse(fn)

    }

    public RSA(int k, int l)
    {
        n = BigInteger.ONE
        fn = BigInteger.ONE

        for (int i = 0; i < k; i++)
            n = n.multiply(generatePrime(l))

        e = new BigInteger(65537)
        d = e.modInverse(fn)

        def writer = new PrintWriter("publicKey", "UTF-8")
        writer.print("$e,$n")
        writer.close()

        writer = new PrintWriter("privateKey", "UTF-8")
        writer.print("$d,$n")
        writer.close()
    }


    public RSA(int k, int bits, int CRT)
    {
        n = BigInteger.ONE
        fn = BigInteger.ONE
        keyPartsMult = BigInteger.ONE

        for (int i = 0; i < k; i++)
            keyParts.add(0)

        def threads = new ArrayList()

        for (int i = 0; i < k; i++)
        {
            def t = new MyThread(bits, i)
            threads.add(t)
            t.start()
        }

        for(MyThread t: threads) t.join();


        //println "N =  " + n


        e = new BigInteger(65537)
        d = e.modInverse(fn)

        def writer = new PrintWriter("publicKey", "UTF-8")
        writer.print("$e,$n")
        writer.close()

        writer = new PrintWriter("privateKey", "UTF-8")
        writer.print("$d,$n")
        writer.close()
    }



    public synchronized BigInteger multiplyN(BigInteger i)
    {
        n = n.multiply(i)
    }





    public synchronized String encrypt(String message)
    {
        new BigInteger(message.getBytes()).modPow(e, n).toString()

    }

    public synchronized BigInteger encrypt(BigInteger message)
    {
        message.modPow(e, n);
    }



    public synchronized String decrypt(String ciphertext)
    {
        new String(new BigInteger(ciphertext).modPow(d, n).toByteArray())
    }

    public synchronized BigInteger decrypt(BigInteger ciphertext){
        ciphertext.modPow(d, n)
    }

    public BigInteger decryptCRT( BigInteger ciphertext)
    {

        for (BigInteger keyPart : keyParts) {

            def t = new Thread() {
                public void run() {

                    def messagePart = (ciphertext.modPow(d.mod(keyPart.subtract(BigInteger.ONE)), keyPart))
                    def mPart = keyPartsMult.divide(keyPart)
                    def mPartInv = mPart.modInverse(keyPart)

                    M = M.add(messagePart
                            .multiply(mPart)
                            .multiply(mPartInv))

                }
            }

            t.start();
            t.join()

        }

        M = M.mod(keyPartsMult)
        return M


        /*make in threads???
        def messageParts = new ArrayList<BigInteger>()

        for (BigInteger keyPart : keyParts)
        {
            def messagePart =  (ciphertext.modPow(d.mod( keyPart.subtract(BigInteger.ONE)), keyPart))

            messageParts.add(messagePart)
        }

        def MParts = new ArrayList<BigInteger>()
        def MPartsInv = new ArrayList<BigInteger>()

        for (BigInteger keyPart : keyParts)
        {

            def mPart = keyPartsMult.divide(keyPart)

            MParts.add(mPart)

            MPartsInv.add(mPart.modInverse(keyPart))
        }

        def M = BigInteger.ZERO

        for (int i = 0; i < messageParts.size(); i++) {

            M = M.add(messageParts.get(i)
                    .multiply(MParts.get(i))
                    .multiply(MPartsInv.get(i)))


        }
        */

        /* println "message in crt"
         println new String(M.toByteArray())*/
    }




    public synchronized void generateKeys(int bits){
        def r = new SecureRandom()

        def p = new BigInteger(bits, 100, r)
        def q = new BigInteger(bits, 100, r)

        n = p.multiply(q)

        def fn = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE))
        e = new BigInteger(65537)


        d = e.modInverse(fn)

        /*def writer = new PrintWriter("publicKey", "UTF-8")
        writer.print("$e,$n")
        writer.close()

        writer = new PrintWriter("privateKey", "UTF-8")
        writer.print("$d,$n")
        writer.close()*/
    }

    public synchronized BigInteger generatePrime(int bits) {

        def r = new SecureRandom()

        def p = new BigInteger(bits, 100, r)

        keyParts.add(p)


        keyPartsMult = keyPartsMult.multiply(p)
        fn = fn.multiply(p.subtract(BigInteger.ONE))

        return p

    }

    public synchronized BigInteger generatePrimeCRT(int bits, int i) {

        def r = new SecureRandom()

        def prime = new BigInteger(bits, 100, r)

        keyParts.set(i, prime)


        keyPartsMult = keyPartsMult.multiply(prime)


        fn = fn.multiply(prime.subtract(BigInteger.ONE))




        return prime

    }





    public static void main(String[] args) {

        def k = 2
        def bits = 512
        def rsa

        /*Traditional TEST*/

        Date date = new Date();
        println  "Traditional started: $date\n"

        rsa = new RSA(k, bits)

        def text1 = "The quick brown fox jumps over the lazy dog";
        println "Plaintext: " + text1
        BigInteger plaintext = new BigInteger(text1.getBytes());

        BigInteger ciphertext = rsa.encrypt(plaintext)
        println "Ciphertext: " + ciphertext

        plaintext = rsa.decrypt(ciphertext)

        def text2 = new String(plaintext.toByteArray())
        println "Decrypted: $text2 \n"


        date = new Date();
        println  "Traditional finishted: $date\n"
        println "==================================================\n"
        /*END TEST*/


        /*decryptCRT*/


        date = new Date();
        println  "CRT started: $date \n"


        rsa = new RSA(k, bits , CRT)

        ciphertext = rsa.encrypt(plaintext)
        plaintext = rsa.decryptCRT(ciphertext)


        date = new Date();


        text2 = new String(plaintext.toByteArray())
        println "Decrypted CRT: $text2\n"
        println  "CRT finishted: $date "
    }


    public static generatePrimesParallel(int primes, int bits)
    {
        def threads = new ArrayList()

        Date date = new Date();



        for (int i = 0; i < primes; i++)
        {
            def t = new PrimeThread(bits)
            t.start()
            threads.add(t)
        }

        for(PrimeThread t: threads) t.join();

        println  "($primes, $bits) Parallel prime genertion started: $date\n"
        date = new Date();
        println  "($primes, $bits) Parallel prime genertion finished: $date\n\n"


    }



}
