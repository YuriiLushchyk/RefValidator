# RefValidator

## Problem statement

<blockquote>
This task is mostly language-agnostic (aside from SQL). Most of our backend is built in PHP 8, though it is ok if you prefer to complete the task in another programming language. Your test DB files should be importable into either MySQL 5.7, or a modern version of MariaDB.
References to the blob rows are contained in the following columns:

* _Body_ and _Header_ in _ProtonMailShard.MessageData_

* _BlobStorageID_ in _ProtonMailShard.Attachment_, _ProtonMailShard.OutsideAttachment_,
  _ProtonMailShard.ContactData_, _ProtonMailGlobal.SentMessage_ and
  _ProtonMailGlobal.SentAttachment_

The references refer to rows in _ProtonMailGlobal.BlobStorage_. These rows have a column named
_NumReferences_, which is the master reference count for that blob.

The objective of this task is to detect inconsistencies in blob reference counts, including count
mismatches, references to missing blobs, and orphan blobs with nonzero reference count.

Note: rows in the BlobStorage table with NumReferences = 0 are fine, they are cleaned up
asynchronously.

**Considerations**

* Efficiency: avoid expensive queries, though this rule can be bent if justified by speed or other
  considerations. Since detecting inconsistencies is a read-only check, we can run it on the
  replication slaves rather than on the masters if it makes sense to do so. Indices can be modified.
* Speed: current order of magnitude of blob number is 10^9.
* Resiliency: assume your solution will be run on a live, non-static collection of databases.
* Scaling: write with parallelization in mind.

**Final notes**

* Take look at the job description to have an idea of what we'll be looking for.
* Please also provide a readme.md file with your comments on the code and clear descriptions of how
  you would run it. You can even comment the exercise itself, if you feel like it.
* We do not require you to provide a fully functioning code. But please comment any shortcut you are
  taking, to let us now what is on purpose.
* Also, you should specify which libraries you'd want to import and how you would do it, if any,
  even if you don't need to provide the full setup.

</blockquote>

## Assumptions and limitation

* Two different sources of data.
* _Efficiency_, _Speed_ and _Scaling_ considerations force to write checks in batches.
* **Important**: _Resiliency_ in this case makes me think that write operation are being performed to 
the database all the time. So false-positive results are always possible without locking all the tables (or taking a snapshot of DB).
So after analysis, I decided to focus on one time check anyway. This check can be performed several times to get consistent results before performing persistent actions. Please take a look at **Possible Solutions** for other ideas.
* In this specific task blobId is incremental Long.

## Solution idea

### General idea

To fetch **existing** blobs with _numReferences_ from _BlobStorage_, fetch **referred** blobs with
_numReferences_ from all datasources combined.
Then compare both structures to find a differences.

_**We have to add indexes to all tables. Index by column that refers to blobId.**_

### Batching

The idea is to split the comparison into smaller pieces. In specific case I decided to split it by
blobId range (minId, maxId).
For each range comparison will be done separately, in different threads (or instances if solution
needs to be scaled).

## Technical details

### Technologies used

* Kotlin. Language I am most comfortable with.
* Spring Boot. Light version for starting application with DB connection and basic parallelization
  capability (@Async).
* JDBC API. "Lightest" database connection library, without ORM, able to execute raw queries. Just
  enough for this solution to work.
* MockK anf JUnit 5 for Unit testing.

### Codebase explanation

* _StartupListener_. Solution entry point on app start-up (after DBs connected and ect.)
* _DataSourceConfig_. Configuration of both datasources.
* _BlobReferencesService_. Service that provides data access to both databases. For simplicity there is only one such service and no Repo level.
In general, if solution is more complex it would have more layers.
* _BlobReferencesValidator_. This component encapsulates validation logic and error reporting.
* _test.kotlin_ contains Unit tests only for critical parts of the solution.

**Please note** I will add simple comments to important parts of code. 


## Possible Solutions
* If blobId wasn't Long but, for example, random String I would go with similar General Idea but used some sort of key-value external storage to make snapshots of blob->numReferences.
Later after taking both snapshots I would compare them in batches. This solution requires more complex infrastructure.
* If _Resiliency_ indeed means not to allow false-positivity in check I think one time check is not right solution. And consistent checking should be implemented, closer to the place where blobs are created/referred.
Possibly on application level or with DB callbacks. This requires more info about blob creation mechanism (delays in updation counter etc).