# RefValidator
<blockquote>
This task is mostly language-agnostic (aside from SQL). Most of our backend is built in PHP 8, though it is ok if you prefer to complete the task in another programming language. Your test DB files should be importable into either MySQL 5.7, or a modern version of MariaDB.
References to the blob rows are contained in the following columns:

* _Body_ and _Header_ in _ProtonMailShard.MessageData_

* _BlobStorageID_ in _ProtonMailShard.Attachment_, _ProtonMailShard.OutsideAttachment_, _ProtonMailShard.ContactData_, _ProtonMailGlobal.SentMessage_ and _ProtonMailGlobal.SentAttachment_

The references refer to rows in _ProtonMailGlobal.BlobStorage_. These rows have a column named _NumReferences_, which is the master reference count for that blob.

The objective of this task is to detect inconsistencies in blob reference counts, including count mismatches, references to missing blobs, and orphan blobs with nonzero reference count.

Note: rows in the BlobStorage table with NumReferences = 0 are fine, they are cleaned up asynchronously.

**Considerations**

* Efficiency: avoid expensive queries, though this rule can be bent if justified by speed or other considerations. Since detecting inconsistencies is a read-only check, we can run it on the replication slaves rather than on the masters if it makes sense to do so. Indices can be modified.
* Speed: current order of magnitude of blob number is 10^9.
* Resiliency: assume your solution will be run on a live, non-static collection of databases.
* Scaling: write with parallelization in mind.

**Final notes**
* Take look at the job description to have an idea of what we'll be looking for.
* Please also provide a readme.md file with your comments on the code and clear descriptions of how you would run it. You can even comment the exercise itself, if you feel like it.
* We do not require you to provide a fully functioning code. But please comment any shortcut you are taking, to let us now what is on purpose.
* Also, you should specify which libraries you'd want to import and how you would do it, if any, even if you don't need to provide the full setup.
</blockquote>

## Problem statement
## Assumptions and limitation
## Solution idea
## Technical details