function divide(a: number, b: number): number {
    if (b === 0) {
        throw new Error("Division by zero is not allowed.");
    }
    return a / b;
}

const E = 2.71828;

// @ts-ignore
module.exports = {
    divide,
    E
};
