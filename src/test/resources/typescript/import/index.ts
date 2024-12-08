// @ts-nocheck
load({
    // language=TypeScript
    script: `
        // const or let doesn't work here
        var SQRT2 = Math.sqrt(2);

        function add(a: number, b: number): number {
            return a + b;
        }
    `,
    name: "load.ts",
});

let loadInternalVal = add(SQRT2, SQRT2);

load("./LoadTest.ts");
let loadExternalVal = subtract(PHI, 1);

import {multiply, PI} from "./ImportTest.ts";

let importVal = multiply(PI, PI);

const math = require('./RequireTest.ts');
let requireVal = math.divide(math.E, 2);

// import doesn't work
const json = require("./JSONTest.json")
let jsonVal = json.result[0].message;

let results = {
    loadInternalVal,
    loadExternalVal,
    importVal,
    requireVal,
    jsonVal
};

results;
