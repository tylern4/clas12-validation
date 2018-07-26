# clas12-validation

Additonal tools for validating CLAS12 offline software (see also https://github.com/naharrison/clas12-offline-software/tree/development/validation)

We have 4 levels of validation:

* Unit tests - these are very quick tests (a few seconds) that run automatically with the maven build. See clas12-offline-software.

* Advanced tests - these tests take a little longer (order of minutes) and have to be manually run by the user when desired. These tests are also run automatically by Travis CI for every change to the clas12-offline-software repository and for every pull-request. See clas12-offline-software.

* Release validation 1 - even more advanced tests that take around an hour.

* Release validation 2 - even more advanced tests that consists of running relatively large scale simulations and reconstruction; might take many hours.
