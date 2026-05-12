INSERT INTO problem_topics VALUES
('For Beginners'),
('Brute Force'),
('Combinatorics'),
('Dynamic Programming'),
('Graphs');

-- EXACT_MATCH problems
INSERT INTO problems
    (title, topic, difficulty, statement, execution_time_limit_ms, execution_memory_limit_kb,
     default_evaluation_type) VALUES
('Simple problem', 'For Beginners', 0,
'Write a program that reads a two-digit integer and outputs both digits separated by a space.
# Input
A single integer n where 10≤n≤99.
# Output
Output the two digits of the number, separated by a space.',
1000, 128*1024, 'EXACT_MATCH'
),
('Median number', 'For Beginners', 0,
'Three integers a, b, c are given, each of which satisfies ∣a∣,∣b∣,∣c∣≤1000.
# Input
Three integers a, b, c are given, each of which satisfies ∣a∣,∣b∣,∣c∣≤1000.
# Output
Print the median among the three numbers.',
1000, 128*1024, 'EXACT_MATCH'
),
('The Vouchers', 'Brute Force', 3,
'A tourist company could not sell n (n<15) vouchers to a ski resort due to severe frosts, and their validity period has already started. To minimize losses, starting from February 1, it was decided to sell all vouchers with dk​ remaining days (dk​≤30) at their nominal value—ck​ (ck​≤100) UAH per day, but only for the days remaining from the day of sale (k=1,…,n).
What is the maximum total income the company can obtain by selling these vouchers if at most one voucher can be sold per day?
# Input
The first line contains the number of vouchers n. Each of the next n lines contains two integers: the number of days dk​ left and the cost per day ck​.
# Output
Print the maximum total income.',
1000, 128*1024, 'EXACT_MATCH'
),
('Certification', 'Combinatorics', 2,
'A mathematics teacher has prepared N problems for certification, each involving the operations +, −, ∗, and :. Each problem contains a certain number of arithmetic operations. To pass certification, each student must solve K problems selected from the list. The selection must be such that, in the chosen sequence, every subsequent problem (with a higher index in the list) contains more operations than the previous one.
Determine the number of different ways to select K problems so that every such sequence satisfies the above condition.
# Input
The first line contains two integers N and K (1≤N≤100, 1≤K≤100), where N is the number of problems prepared, and K is the number required to pass certification.
The next N lines each contain a single integer: the number of arithmetic operations in the i-th problem. The number of operations in each problem does not exceed 1000.
# Output
Output a single integer: the number of valid ways to choose K problems according to the condition. Two ways are considered different if they differ by at least one problem. If there are no such ways, output -1.',
1000, 64*1024, 'EXACT_MATCH'
),
('Water supply', 'Graphs', 0,
'The city consists of n districts (1≤n≤100). Each district has a well for sourcing water. Every pair of wells is connected by a pipe, and water can flow through each pipe in only one direction. Due to an energy crisis, only one well can operate at any given time. Since the system was not designed for this mode of operation, some districts may sometimes be left without water.
Determine whether it is possible to ensure continuous water supply to the entire city by reversing the direction of water flow in all pipes connected to a single well.
# Input
The first line contains the number of districts n. The following n lines describe each well: the number of wells from which it receives water, followed by their indices. Wells are numbered from 1 to n.
# Output
Output a single integer: 1 if it is possible to provide continuous water supply for the city, or 0 otherwise.',
1000, 64*1024, 'EXACT_MATCH'
),
('Gardener-painter', 'Combinatorics', 1,
'After planting the trees, the gardener needs to paint them. He has three colors of paint: white, blue, and orange. He must paint the N trees so that no two adjacent trees have the same color. In how many different ways can the gardener paint the trees?
# Input
The number of trees N (1≤N≤50).
# Output
Output the number of possible ways to paint the trees.',
1000, 64*1024, 'EXACT_MATCH'
),
('Fibonacci Numbers', 'Dynamic Programming', 0,
'Which is the largest Fibonacci number that can be constructed using the available set of digits C0​,C1​,C2​,…,C9​, where C0​ is the number of digit 0s, C1​ is the number of digit 1s, ..., C9​ is the number of digit 9s?
# Input
A single line contains 10 integers, each representing the number of times the corresponding digit appears in the set. All input numbers do not exceed 100.
# Output
Output the index of the largest Fibonacci number that can be constructed, or −1 if it is impossible to construct such a number using the given digits.',
1000, 64*1024, 'EXACT_MATCH'
);

-- SCRIPT_CHECKER problems
INSERT INTO script_checkers (name, language_id, code) VALUES
(
'Default Script Checker', 3,
'output = input()
input_data = input()
expected_output = input()

if expected_output == output:
    print(True)
else:
    print(False)
'
);

INSERT INTO problems
    (title, topic, difficulty, statement, execution_time_limit_ms, execution_memory_limit_kb,
     default_evaluation_type, default_script_checker_id) VALUES
('Test Script Checker Problem', 'For Beginners', 0,
  'Write a program that reads a two-digit integer and outputs both digits separated by a space.
  # Input
  A single integer n where 10≤n≤99.
  # Output
  Output the two digits of the number, separated by a space.',
  1000, 128*1024, 'SCRIPT_CHECK', 1
);

-- Tests
INSERT INTO tests
    (problem_id, ordinal, is_example, input_data, expected_output) VALUES
(1, 1, true, '11', '1 1'),
(1, 2, true, '67', '6 7'),
(1, 3, false, '14', '1 4'),
(1, 4, false, '41', '4 1'),
(1, 5, false, '45', '4 5');

INSERT INTO tests
(problem_id, ordinal, is_example, input_data, expected_output) VALUES
(8, 1, true, '11', '1 1'),
(8, 2, true, '67', '6 7'),
(8, 3, false, '14', '1 4'),
(8, 4, false, '41', '4 1'),
(8, 5, false, '45', '4 5');