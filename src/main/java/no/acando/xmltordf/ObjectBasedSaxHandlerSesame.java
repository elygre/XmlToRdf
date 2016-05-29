/*
Copyright 2016 ACANDO AS

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package no.acando.xmltordf;

import org.openrdf.IsolationLevels;
import org.openrdf.model.*;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.event.NotifyingRepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.memory.MemoryStore;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;


public class ObjectBasedSaxHandlerSesame extends ObjectBasedSaxHandler {


    Repository repository;
    private ArrayBlockingQueue<Statement> queue = new ArrayBlockingQueue<>(10000, false);
    private boolean notDone = true;
    private Thread repoThread;


    private final Statement EndOfFileStatement = SimpleValueFactory.getInstance().createStatement(
        SimpleValueFactory.getInstance().createIRI(EndOfFile),
        SimpleValueFactory.getInstance().createIRI(EndOfFile),
        SimpleValueFactory.getInstance().createIRI(EndOfFile)
    );

    private SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();

    public ObjectBasedSaxHandlerSesame(Builder.ObjectBased builder) {
        super(null, builder);



        MemoryStore memoryStore = new MemoryStore();
        memoryStore.initialize();

        this.builder = builder;
        Thread thread = Thread.currentThread();
        repoThread = new Thread() {
            @Override
            public void run() {

                NotifyingSailConnection connection = memoryStore.getConnection();
                connection.begin(IsolationLevels.NONE);


                    while (notDone || !queue.isEmpty()) {
                        try {
                            Statement take = queue.take();
                            if(take != EndOfFileStatement) {
                                connection.addStatement(take.getSubject(), take.getPredicate(), take.getObject());
                            }

                        } catch (InterruptedException e) {
                            //e.printStackTrace();
                        }
                    }


                    connection.commit();

                connection.close();

                repository = new SailRepository(memoryStore);



            }
        };

        repoThread.start();
    }


    public String createTriple(String subject, String predicate, String objectResource) {


        IRI predicateNode = valueFactory.createIRI(predicate);
        Resource subjectNode = null;
        Resource objectNode = null;


        if (!subject.startsWith("_:")) {
            subjectNode = valueFactory.createIRI(subject);

        } else {
            subjectNode = valueFactory.createBNode(subject);

        }

        if (!objectResource.startsWith("_:")) {
            objectNode = valueFactory.createIRI(objectResource);

        } else {
            objectNode = valueFactory.createBNode(objectResource);

        }


        try {
            queue.put(valueFactory.createStatement(subjectNode, predicateNode, objectNode));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;

    }

    public String createTripleLiteral(String subject, String predicate, String objectLiteral, IRI datatype) {
        if (objectLiteral == null) {
            return null;
        }

        IRI predicateNode = valueFactory.createIRI(predicate);
        Resource subjectNode;


        if (!subject.startsWith("_:")) {
            subjectNode = valueFactory.createIRI(subject);

        } else {
            subjectNode = valueFactory.createBNode(subject);

        }


        Literal literal = valueFactory.createLiteral(objectLiteral, datatype);

        try {
            queue.put(valueFactory.createStatement(subjectNode, predicateNode, literal));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String createTripleLiteral(String subject, String predicate, String objectLiteral) {
        if (objectLiteral == null) {
            return null;
        }

        IRI predicateNode = valueFactory.createIRI(predicate);
        Resource subjectNode = null;


        if (!subject.startsWith("_:")) {
            subjectNode = valueFactory.createIRI(subject);

        } else {
            subjectNode = valueFactory.createBNode(subject);

        }


        Literal literal = valueFactory.createLiteral(objectLiteral);

        if (builder.autoTypeLiterals) {
            try {
                literal = valueFactory.createLiteral(Integer.parseInt(objectLiteral));
            } catch (Exception e) {
                try {
                    literal = valueFactory.createLiteral(Long.parseLong(objectLiteral));
                } catch (Exception ee) {
                }
            }

        }

        try {
            queue.put(valueFactory.createStatement(subjectNode, predicateNode, literal));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;


    }

    public String createTripleLiteral(String subject, String predicate, long objectLong) {

        IRI predicateNode = valueFactory.createIRI(predicate);
        Resource subjectNode = null;


        if (!subject.startsWith("_:")) {
            subjectNode = valueFactory.createIRI(subject);

        } else {
            subjectNode = valueFactory.createBNode(subject);

        }


        Literal literal = valueFactory.createLiteral(objectLong);


        try {
            queue.put(valueFactory.createStatement(subjectNode, predicateNode, literal));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;


    }

    @Override
    public void endDocument() throws SAXException {

        notDone = false;


        queue.add(EndOfFileStatement);

        try {
            repoThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
