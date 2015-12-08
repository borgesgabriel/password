# A password hardening scheme

> An implementation of Monrose, Fabian, Michael K. Reiter, and Susanne Wetzel. "Password hardening based on keystroke dynamics." *International Journal of Information Security* 1.2 (2002): 69-83.

#### About the project
* Entirely done in [Kotlin](http://kotlinlang.org)
* I used IDEA, but building/running using command line should be fine

#### Running
* `./gradlew build`
* There are two ways of running this:
	* Interactive. Class `es.gabrielborg.keystroke.App`.
	* Benchmark. Dataset is a subset of [Killourhy's](http://www.cs.cmu.edu/~keystroke/). Class `es.gabrielborg.keystroke.Benchmark`.

### In order to give a quick explanation of how the system works, a few things should be considered first:

* The idea here is to provide a way to *harden* a password with biometric information (duration and latency of keystrokes), producing a hardened password;
* Whilst such hardened password should remain stable throughout logins (it may be used as a key in a symmetric encryption algorithm, for instance), the scheme should adapt to users' typing patterns, as time passes;
* In order to simplify implementation, we assume all users choose passwords with the same length;
* A *feature* is either a duration of a keystroke or the latency between keystrokes. In our implementation, there were `2*password.length - 1` features (`password.length` for duration, `password.length - 1` for latency);
* A global list of thresholds for each feature is defined. This means that for every login attempt, we can compare each feature of that login to the threshold, and decide whether the attempt was faster or slower than the threshold;
* A *distinguishing feature* is, for a certain user, a feature that can be used to identify them. If a feature is distinguishing, the user consistently performs either above or below that threshold for that feature. We can tweak this threshold of "consistency" by changing other system parameters;
* In the first few logins, there isn't enough biometric information 
* A secret sharing scheme is a way to divide a secret into n parts. A threshold `k <= n` is also defined --- at least `k` parts are needed to reconstruct the secret (providing more parts than necessary doesn't give out any additional information), whereas possessing fewer than `k` parts yields no information about the secret. The scheme used was [Shamir's](https://en.wikipedia.org/wiki/Shamir%27s_Secret_Sharing), which is based on polynomial interpolation.

### Two data structures will be used in this scheme:

* A table that, given a password and the biometric information entered with it, is used to yield the hardened password, if a) the passwords match; and b) keystroke data is similar enough to the previous successful logins. This table contains m rows, where m is the number of features. Each row contains two pieces of information. A piece of information can either be a share of the secret (see the secret sharing scheme above), or just garbage. When logging in, we'll decide, for each feature, which of the two pieces of information to use comparing that feature to the global threshold: if it is faster than the threshold, we choose the left side. If it is slower, we choose the right side;
	* The reasoning behind this is that if a feature is distinguishing, then we expect a genuine login to perform above or below the threshold. We'll then put a piece of the secret where we expect the genuine login to land. That way, if a login matches what we expect for that feature, the user gets another piece of the secret. If they have enough pieces, the hardened password is unlocked;
	* If a feature is not distinguishing, then we cannot expect anything from its value. We then put valid pieces of the secret in both sides of the table. That way, no matter whether the user performs slower or faster than the threshold, they'll end up with a valid share;
	* Finally, because of how the secret sharing scheme works, there's no way the user knows whether they've got a valid piece of the secret or just a bogus value. In fact, if the challenger doesn't meet the threshold for the secret scheme, we'll still get a "hardened password" from the interpolation. It won't decrypt the next data structure properly, however.
* A structure to hold a history of successful login attempts. This will be used to compute statistical information about the features, determining which are *distinguishing*, and reconstructing the previous table at each successful login. This history file contains h entries, where h is the number of successful logins to that account so far (there is a limit to h; newer entries may remove older ones). This will be encrypted with the user's hardened password.
	* This file should have a constant size, so that an attacker cannot infer any information about the number of logins based on that analysis. We do that by padding the file up to a fixed size;
	* There should also be a way to find out if the file's been decrypted successfully, and whether it really belongs to the user trying to log in. The way we do that is by placing a known header in the file; we also append the username to it. That way we can ensure the decryption went through correctly, and that the hardened password we used is valid.

### Procedures:

#### An enrolment goes as follows:

* No biometrics are gathered at this point. The user simply provides a username and password `pwd` to enrol;
* A polynomial of degree `m - 1` with random integer coefficients is chosen. We let the constant term be the hardened password `hpwd`;
* We construct the *instruction table* (first data structure mentioned above) using only valid information from the polynomial -- this means anyone logging in with `pwd` should be able to go through;
	* For details on how each element is constructed, refer to the paper and/or to my implementation.
* We construct the *history file*. It contains the header, the username and the number of successful logins: 0.

#### A login goes as follows:

* The user provides a username, a password `pwd` and a list of features for that password `featureList`;
* We open the instruction table and compare each feature in `featureList` with the global threshold, choosing the left side of the instruction table for that feature if it is faster than the threshold, or the right side otherwise. We now have `m` points, amongst which there are some valid shares of the secret `hpwd`;
* We do polynomial interpolation to try and find the secret. If we hold fewer than `m` shares of the secret, then the interpolation will yield a value different than the expected. Say we got back `hpwd'` from the interpolation;
* We try and decrypt the history file. If both header and username match the expected, then `hpwd' = hpwd`, and the login should go through. If anything other than that is yielded, then the login is halted and an error message is displayed back to the user;
*Assuming the login went through...*
* We append `featureList` to our history file (as this is a valid login and should be recorded as such) and compute things such as the mean and standard deviation for each feature on the history. This helps determine which features are distinguishing, and if so, towards which "direction" (being slower or faster than the threshold);
* We now need to recreate the instruction table. A new random polynomial is created, subject to its constant term being `hpwd`;
* For each feature, we check if they're distinguishing, and if so, we place a share of the secret where we expect a login to land. On the other side, we place any value that **isn't** the secret. If the feature isn't distinguishing, we simply put valid shares of the secret on both sides of the table, to ensure that feature won't change the outcome of a future login;
* We encrypt back the history file and save it to disk. The system can process a new login.

