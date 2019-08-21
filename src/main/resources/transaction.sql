TRANSACTION
SELECT balance FROM Accounts WHERE accountId='{0}' AND balance>= '{2}';
SELECT balance FROM Accounts WHERE accountId='{1}';
UPDATE Accounts SET balance=balance-'{0}' WHERE accountId='{1}';
UPDATE Accounts SET balance=balance+'{0}' WHERE accountId='{1}';
COMMIT;
ROLLBACK;
